import React, {useEffect, useRef, useState} from 'react';

export interface BackgroundMusicProps {
  readonly url: string;
  readonly isPlaying: boolean;
  readonly isMuted?: boolean;
  readonly volume?: number;
}

const DEFAULT_VOLUME = 0.15;
const DECLICK_RAMP_SEC = 0.025;
const VOLUME_TIME_CONSTANT_SEC = 0.015;

export function BackgroundMusic({
  url,
  isPlaying,
  isMuted = false,
  volume = DEFAULT_VOLUME,
}: BackgroundMusicProps): React.ReactElement | null {
  const audioCtxRef = useRef<AudioContext | null>(null);
  const masterGainRef = useRef<GainNode | null>(null);
  const sourceNodeRef = useRef<AudioBufferSourceNode | null>(null);
  const trackGainRef = useRef<GainNode | null>(null);
  const fallbackAudioRef = useRef<HTMLAudioElement | null>(null);

  const isPlayingRef = useRef(isPlaying);
  const [useFallback, setUseFallback] = useState(false);
  const [platformMuted, setPlatformMuted] = useState(false);

  const effectiveMuted = isMuted || platformMuted;

  useEffect(() => {
    isPlayingRef.current = isPlaying;
  }, [isPlaying]);

  useEffect(() => {
    const handlePlatformMute = (e: Event) => {
      const customEvent = e as CustomEvent<{muted: boolean}>;
      if (typeof customEvent.detail?.muted === 'boolean') {
        setPlatformMuted(customEvent.detail.muted);
      }
    };
    window.addEventListener('playground-mute', handlePlatformMute);
    return () => {
      window.removeEventListener('playground-mute', handlePlatformMute);
    };
  }, []);

  const getOrCreateContext = (): AudioContext | null => {
    if (typeof window === 'undefined') return null;
    if (audioCtxRef.current && audioCtxRef.current.state !== 'closed') {
      return audioCtxRef.current;
    }

    const AudioContextClass =
      window.AudioContext ||
      (window as unknown as {webkitAudioContext?: typeof AudioContext})
        .webkitAudioContext;
    if (!AudioContextClass) {
      setUseFallback(true);
      return null;
    }

    try {
      const ctx = new AudioContextClass();
      const masterGain = ctx.createGain();
      const targetGain = effectiveMuted ? 0 : Math.max(0, Math.min(1, volume));
      masterGain.gain.setValueAtTime(targetGain, ctx.currentTime);
      masterGain.connect(ctx.destination);

      audioCtxRef.current = ctx;
      masterGainRef.current = masterGain;

      const unlock = () => {
        if (ctx.state === 'suspended') {
          void ctx.resume().catch(() => {});
        }
      };

      window.addEventListener('pointerdown', unlock, {passive: true, once: true});
      window.addEventListener('keydown', unlock, {passive: true, once: true});
      window.addEventListener('touchstart', unlock, {passive: true, once: true});

      return ctx;
    } catch {
      setUseFallback(true);
      return null;
    }
  };

  const stopWebAudioSource = (): void => {
    if (sourceNodeRef.current) {
      try {
        sourceNodeRef.current.stop();
        sourceNodeRef.current.disconnect();
      } catch (_) {}
      sourceNodeRef.current = null;
    }
    if (trackGainRef.current) {
      try {
        trackGainRef.current.disconnect();
      } catch (_) {}
      trackGainRef.current = null;
    }
  };

  useEffect(() => {
    if (!url) {
      stopWebAudioSource();
      return;
    }

    let isCancelled = false;
    const abortController = new AbortController();

    fetch(url, {mode: 'cors', credentials: 'omit', signal: abortController.signal})
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP error ${res.status}`);
        return res.arrayBuffer();
      })
      .then((arrayBuffer) => {
        const ctx = getOrCreateContext();
        if (!ctx) throw new Error('AudioContext unavailable');
        return ctx.decodeAudioData(arrayBuffer);
      })
      .then((decodedBuffer) => {
        if (isCancelled || abortController.signal.aborted) return;
        const ctx = getOrCreateContext();
        const masterGain = masterGainRef.current;
        if (!ctx || !masterGain) return;

        stopWebAudioSource();

        const source = ctx.createBufferSource();
        source.buffer = decodedBuffer;
        source.loop = true;

        const trackGain = ctx.createGain();
        trackGain.gain.setValueAtTime(0, ctx.currentTime);
        trackGain.gain.linearRampToValueAtTime(1, ctx.currentTime + DECLICK_RAMP_SEC);

        source.connect(trackGain);
        trackGain.connect(masterGain);

        source.start(0);
        sourceNodeRef.current = source;
        trackGainRef.current = trackGain;
      })
      .catch(() => {
        if (isCancelled || abortController.signal.aborted) return;
        setUseFallback(true);
      });

    return () => {
      isCancelled = true;
      abortController.abort();
      stopWebAudioSource();
    };
  }, [url]);

  useEffect(() => {
    if (useFallback) {
      const audio = fallbackAudioRef.current;
      if (!audio) return;
      if (isPlaying) {
        void audio.play().catch(() => {});
      } else {
        audio.pause();
      }
      return;
    }

    const ctx = audioCtxRef.current;
    if (!ctx) return;

    if (isPlaying) {
      if (ctx.state === 'suspended') {
        void ctx.resume().catch(() => {});
      }
    } else {
      if (ctx.state === 'running') {
        void ctx.suspend().catch(() => {});
      }
    }
  }, [isPlaying, useFallback]);

  useEffect(() => {
    const targetGain = effectiveMuted ? 0 : Math.max(0, Math.min(1, volume));

    if (useFallback) {
      if (fallbackAudioRef.current) {
        fallbackAudioRef.current.muted = effectiveMuted;
        fallbackAudioRef.current.volume = targetGain;
      }
      return;
    }

    const ctx = audioCtxRef.current;
    const masterGain = masterGainRef.current;
    if (ctx && masterGain && ctx.state !== 'closed') {
      masterGain.gain.setTargetAtTime(
        targetGain,
        ctx.currentTime,
        VOLUME_TIME_CONSTANT_SEC,
      );
    }
  }, [effectiveMuted, volume, useFallback]);

  if (useFallback && url) {
    return (
      <audio
        ref={fallbackAudioRef}
        src={url}
        loop
        autoPlay={isPlaying}
        style={{display: 'none'}}
      />
    );
  }

  return null;
}

export default BackgroundMusic;
