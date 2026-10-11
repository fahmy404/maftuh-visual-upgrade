// tslint:disable
/* eslint-disable */
import React, {
  useEffect,
  useRef,
  useState,
  type MutableRefObject,
  type RefObject,
} from 'react';

type OverlayTone = 'neutral' | 'danger' | 'victory';

interface OverlayProps {
  title: string;
  description?: string;
  actionLabel: string;
  onAction: () => void;
  icon?: React.ReactNode;
  tone?: OverlayTone;
  autoFocus?: boolean;
}

const TONE_CLASS: Record<OverlayTone, string> = {
  neutral: 'bg-zinc-950/90',
  danger: 'bg-red-950/90',
  victory: 'bg-amber-950/90',
};

const BUTTON_CLASS: Record<OverlayTone, string> = {
  neutral: 'hover:bg-amber-400',
  danger: 'hover:bg-red-400',
  victory: 'hover:bg-amber-400',
};

export function useAutoFocus<T extends HTMLElement>(enabled = true) {
  const ref = useRef<T>(null);
  useEffect(() => {
    if (enabled && ref.current) {
      ref.current.focus();
    }
  }, [enabled]);
  return ref;
}

export function Overlay({
  title,
  description,
  actionLabel,
  onAction,
  icon,
  tone = 'neutral',
  autoFocus = true,
}: OverlayProps) {
  const buttonRef = useAutoFocus<HTMLButtonElement>(autoFocus);

  return (
    <div className={`absolute inset-0 ${TONE_CLASS[tone]} overflow-y-auto touch-auto z-50 flex items-center justify-center p-4 backdrop-blur-md`}>
      <div className="flex flex-col items-center w-full max-w-md bg-zinc-900/95 border border-amber-500/30 rounded-2xl p-6 md:p-8 shadow-2xl text-center">
        <h1 className="text-2xl md:text-4xl font-black mb-3 text-amber-400 font-sans tracking-wide">
          {title}
        </h1>
        {description && (
          <p className="text-sm md:text-base text-zinc-300 mb-6 leading-relaxed">
            {description}
          </p>
        )}
        <button
          ref={buttonRef}
          autoFocus={autoFocus}
          onClick={onAction}
          data-testid="start-button"
          className={`bg-amber-500 text-zinc-950 px-6 py-3 rounded-xl font-bold text-base md:text-lg transition-all shadow-lg flex items-center gap-2 active:scale-95 ${BUTTON_CLASS[tone]}`}>
          {icon}
          {actionLabel}
        </button>
      </div>
    </div>
  );
}

export interface MenuOverlayProps {
  children: React.ReactNode;
  tone?: OverlayTone;
  zIndex?: number;
}

export function MenuOverlay({
  children,
  tone = 'neutral',
  zIndex = 40,
}: MenuOverlayProps) {
  return (
    <div
      className={`absolute inset-0 ${TONE_CLASS[tone]} overflow-y-auto touch-auto backdrop-blur-sm`}
      style={{zIndex}}>
      <div className="min-h-full flex flex-col items-center p-4">
        <div className="m-auto w-full max-w-lg flex flex-col items-center">
          {children}
        </div>
      </div>
    </div>
  );
}

export type InputAction =
  | 'left'
  | 'right'
  | 'up'
  | 'down'
  | 'confirm'
  | 'cancel'
  | 'tap';

export interface PointerState {
  x: number;
  y: number;
  down: boolean;
}

export interface InputSnapshot {
  actions: Record<InputAction, boolean>;
  pointer: PointerState;
}

interface LogicalSize {
  width: number;
  height: number;
}

const ACTION_KEYS: Record<string, InputAction> = {
  ArrowLeft: 'left',
  ArrowRight: 'right',
  ArrowUp: 'up',
  ArrowDown: 'down',
  a: 'left',
  d: 'right',
  w: 'up',
  s: 'down',
  Enter: 'confirm',
  ' ': 'confirm',
  Escape: 'cancel',
};

const createEmptyActions = (): Record<InputAction, boolean> => ({
  left: false,
  right: false,
  up: false,
  down: false,
  confirm: false,
  cancel: false,
  tap: false,
});

const getPointerPosition = (
  event: PointerEvent,
  canvas: HTMLCanvasElement,
  logicalSize?: LogicalSize,
): {x: number; y: number} => {
  const rect = canvas.getBoundingClientRect();
  const width = logicalSize?.width ?? rect.width;
  const height = logicalSize?.height ?? rect.height;
  const x = (event.clientX - rect.left) * (width / rect.width);
  const y = (event.clientY - rect.top) * (height / rect.height);
  return {x, y};
};

export const useInput = (
  canvasRef: RefObject<HTMLCanvasElement>,
  enabled = true,
  logicalSize?: LogicalSize,
): MutableRefObject<InputSnapshot> => {
  const inputRef = useRef<InputSnapshot>({
    actions: createEmptyActions(),
    pointer: {x: 0, y: 0, down: false},
  });

  useEffect(() => {
    if (!enabled) {
      inputRef.current.actions = createEmptyActions();
      inputRef.current.pointer = {x: 0, y: 0, down: false};
      return;
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      const action = ACTION_KEYS[event.key];
      if (!action) return;
      if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', ' '].includes(event.key)) {
        event.preventDefault();
      }
      inputRef.current.actions[action] = true;
    };

    const handleKeyUp = (event: KeyboardEvent) => {
      const action = ACTION_KEYS[event.key];
      if (!action) return;
      inputRef.current.actions[action] = false;
    };

    const canvas = canvasRef.current;
    const handlePointerDown = (event: PointerEvent) => {
      if (!canvas) return;
      if (typeof canvas.setPointerCapture === 'function') {
        canvas.setPointerCapture(event.pointerId);
      }
      inputRef.current.pointer = {
        ...getPointerPosition(event, canvas, logicalSize),
        down: true,
      };
      inputRef.current.actions.tap = true;
    };

    const handlePointerMove = (event: PointerEvent) => {
      if (!canvas) return;
      inputRef.current.pointer = {
        ...getPointerPosition(event, canvas, logicalSize),
        down: inputRef.current.pointer.down,
      };
    };

    const handlePointerUp = (event: PointerEvent) => {
      if (!canvas) return;
      if (
        typeof canvas.releasePointerCapture === 'function' &&
        canvas.hasPointerCapture(event.pointerId)
      ) {
        canvas.releasePointerCapture(event.pointerId);
      }
      inputRef.current.pointer = {
        ...getPointerPosition(event, canvas, logicalSize),
        down: false,
      };
      inputRef.current.actions.tap = false;
    };

    const handleBlur = () => {
      inputRef.current.actions = createEmptyActions();
      if (inputRef.current.pointer) {
        inputRef.current.pointer.down = false;
      }
      inputRef.current.actions.tap = false;
    };

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'hidden') {
        handleBlur();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    window.addEventListener('keyup', handleKeyUp);
    window.addEventListener('blur', handleBlur);
    document.addEventListener('visibilitychange', handleVisibilityChange);

    if (canvas) {
      canvas.addEventListener('pointerdown', handlePointerDown);
      canvas.addEventListener('pointermove', handlePointerMove);
      canvas.addEventListener('pointerup', handlePointerUp);
      canvas.addEventListener('pointercancel', handlePointerUp);
    }

    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('keyup', handleKeyUp);
      window.removeEventListener('blur', handleBlur);
      document.removeEventListener('visibilitychange', handleVisibilityChange);

      if (canvas) {
        canvas.removeEventListener('pointerdown', handlePointerDown);
        canvas.removeEventListener('pointermove', handlePointerMove);
        canvas.removeEventListener('pointerup', handlePointerUp);
        canvas.removeEventListener('pointercancel', handlePointerUp);
      }
    };
  }, [canvasRef, enabled]);

  return inputRef;
};

interface Beat {
  ctx: AudioContext;
  out: GainNode;
  now: number;
}

class GameAudio {
  private ctx: AudioContext | null = null;
  private master: GainNode | null = null;
  private sfxMaster: GainNode | null = null;
  private muted = false;
  private volume = 0.6;

  constructor() {
    if (typeof window !== 'undefined') {
      window.addEventListener('playground-mute', (e: Event) => {
        const customEvent = e as CustomEvent<{muted: boolean}>;
        this.setMuted(customEvent.detail.muted);
      });
    }
  }

  private ensure(): AudioContext | null {
    if (this.ctx) return this.ctx;
    if (typeof window === 'undefined') return null;

    const Ctor =
      window.AudioContext ||
      (window as unknown as {webkitAudioContext?: typeof AudioContext})
        .webkitAudioContext;
    if (!Ctor) return null;

    this.ctx = new Ctor();
    this.master = this.ctx.createGain();
    this.sfxMaster = this.ctx.createGain();
    this.master.gain.setValueAtTime(this.volume, this.ctx.currentTime);
    this.sfxMaster.gain.setValueAtTime(
      this.muted ? 0 : 1,
      this.ctx.currentTime,
    );
    this.sfxMaster.connect(this.master);
    this.master.connect(this.ctx.destination);
    this.attachUnlock();
    return this.ctx;
  }

  private attachUnlock() {
    if (typeof window === 'undefined') return;
    const unlock = () => {
      void this.ctx?.resume().catch(() => {});
      window.removeEventListener('pointerdown', unlock);
      window.removeEventListener('keydown', unlock);
      window.removeEventListener('touchstart', unlock);
    };
    window.addEventListener('pointerdown', unlock);
    window.addEventListener('keydown', unlock);
    window.addEventListener('touchstart', unlock);
  }

  resume() {
    const ctx = this.ensure();
    if (ctx && ctx.state === 'suspended') void ctx.resume().catch(() => {});
  }

  private begin(): Beat | null {
    const ctx = this.ensure();
    const out = this.sfxMaster;
    if (!ctx || !out || this.muted) return null;
    if (ctx.state === 'suspended') void ctx.resume().catch(() => {});
    return {ctx, out, now: ctx.currentTime};
  }

  setVolume(v: number) {
    this.volume = Math.max(0, Math.min(1, v));
    if (this.master && this.ctx && !this.muted) {
      this.master.gain.setValueAtTime(this.volume, this.ctx.currentTime);
    }
  }

  toggleMute(): boolean {
    this.setMuted(!this.muted);
    return this.muted;
  }

  setMuted(muted: boolean) {
    this.muted = muted;
    if (this.sfxMaster && this.ctx) {
      this.sfxMaster.gain.setValueAtTime(muted ? 0 : 1, this.ctx.currentTime);
    }
  }

  getMuteState(): boolean {
    return this.muted;
  }

  private blip(
    b: Beat,
    at: number,
    o: {
      freq: number;
      freqTo?: number;
      dur: number;
      type?: OscillatorType;
      gain?: number;
      glide?: 'exp' | 'lin';
    },
  ) {
    const {ctx, out} = b;
    const osc = ctx.createOscillator();
    const g = ctx.createGain();

    osc.type = o.type ?? 'square';
    osc.frequency.setValueAtTime(o.freq, at);
    if (o.freqTo !== undefined) {
      if ((o.glide ?? 'exp') === 'lin') {
        osc.frequency.linearRampToValueAtTime(o.freqTo, at + o.dur);
      } else {
        osc.frequency.exponentialRampToValueAtTime(
          Math.max(1, o.freqTo),
          at + o.dur,
        );
      }
    }

    const peak = o.gain ?? 0.15;
    g.gain.setValueAtTime(peak, at);
    g.gain.exponentialRampToValueAtTime(0.0001, at + o.dur);

    osc.connect(g);
    g.connect(out);
    osc.start(at);
    osc.stop(at + o.dur + 0.02);
  }

  private noiseBurst(
    b: Beat,
    at: number,
    o: {dur: number; gain?: number; from?: number; to?: number},
  ) {
    const {ctx, out} = b;
    const len = Math.max(1, Math.floor(ctx.sampleRate * o.dur));
    const buffer = ctx.createBuffer(1, len, ctx.sampleRate);
    const data = buffer.getChannelData(0);
    for (let i = 0; i < len; i++) data[i] = Math.random() * 2 - 1;

    const src = ctx.createBufferSource();
    src.buffer = buffer;

    const filter = ctx.createBiquadFilter();
    filter.type = 'lowpass';
    filter.frequency.setValueAtTime(o.from ?? 1000, at);
    filter.frequency.exponentialRampToValueAtTime(
      Math.max(1, o.to ?? 80),
      at + o.dur,
    );

    const g = ctx.createGain();
    g.gain.setValueAtTime(o.gain ?? 0.18, at);
    g.gain.exponentialRampToValueAtTime(0.0001, at + o.dur);

    src.connect(filter);
    filter.connect(g);
    g.connect(out);
    src.start(at);
    src.stop(at + o.dur);
  }

  private vary(amount = 0.05): number {
    return 1 + (Math.random() * 2 - 1) * amount;
  }

  playTone(o: {
    type?: OscillatorType;
    freq: number;
    freqTo?: number;
    dur?: number;
    gain?: number;
  }) {
    const b = this.begin();
    if (!b) return;
    this.blip(b, b.now, {
      type: o.type,
      freq: o.freq,
      freqTo: o.freqTo,
      dur: o.dur ?? 0.15,
      gain: o.gain,
    });
  }

  playCrispClick() {
    const b = this.begin();
    if (!b) return;
    const v = this.vary();
    this.blip(b, b.now, { type: 'sine', freq: 1200 * v, dur: 0.015, gain: 0.1 });
  }

  playConfirmSelect() {
    const b = this.begin();
    if (!b) return;
    const v = this.vary();
    this.blip(b, b.now, { type: 'sine', freq: 523.25 * v, dur: 0.1, gain: 0.08 });
    this.blip(b, b.now + 0.08, { type: 'sine', freq: 783.99 * v, dur: 0.15, gain: 0.1 });
  }

  playErrorBuzz() {
    const b = this.begin();
    if (!b) return;
    const v = this.vary();
    this.blip(b, b.now, { type: 'sawtooth', freq: 150 * v, dur: 0.2, gain: 0.08 });
    this.blip(b, b.now + 0.08, { type: 'sawtooth', freq: 159 * v, dur: 0.2, gain: 0.08 });
  }

  playFootstepStone() {
    const b = this.begin();
    if (!b) return;
    const v = this.vary();
    this.noiseBurst(b, b.now, { dur: 0.05, gain: 0.08, from: 1000 * v, to: 600 * v });
    this.blip(b, b.now, { type: 'triangle', freq: 300 * v, dur: 0.03, gain: 0.04 });
  }

  playFootstepWood() {
    const b = this.begin();
    if (!b) return;
    const v = this.vary();
    this.blip(b, b.now, { type: 'triangle', freq: 180 * v, freqTo: 60 * v, dur: 0.09, gain: 0.1 });
    this.noiseBurst(b, b.now, { dur: 0.04, gain: 0.05, from: 800, to: 300 });
  }

  playPowerupChime() {
    const b = this.begin();
    if (!b) return;
    const v = this.vary();
    const notes = [523.25, 659.25, 783.99, 1046.50, 1318.51];
    notes.forEach((freq, i) => {
      this.blip(b, b.now + i * 0.06, { type: 'sine', freq: freq * v, dur: 0.15, gain: 0.06 });
    });
  }

  playVictoryFanfare() {
    const b = this.begin();
    if (!b) return;
    const v = this.vary();
    const notes = [261.63, 329.63, 392.00, 523.25];
    notes.forEach((freq, i) => {
      this.blip(b, b.now + i * 0.1, { type: 'sawtooth', freq: freq * v, dur: 0.4, gain: 0.06 });
      this.blip(b, b.now + i * 0.1, { type: 'sine', freq: freq * v, dur: 0.4, gain: 0.04 });
    });
    const chordTime = notes.length * 0.1;
    notes.forEach((freq) => {
      this.blip(b, b.now + chordTime, { type: 'triangle', freq: freq * v, dur: 0.8, gain: 0.06 });
    });
  }

  playDefeatMotif() {
    const b = this.begin();
    if (!b) return;
    const v = this.vary();
    const notes = [311.13, 261.63, 246.94, 196.00];
    notes.forEach((freq, i) => {
      this.blip(b, b.now + i * 0.15, { type: 'sawtooth', freq: freq * v, dur: 0.3, gain: 0.08 });
      this.blip(b, b.now + i * 0.15, { type: 'sine', freq: freq * 0.5 * v, dur: 0.35, gain: 0.06 });
    });
  }
}

export const gameAudio = new GameAudio();

export const clamp = (value: number, min: number, max: number): number => {
  return Math.max(min, Math.min(max, value));
};

export const shuffle = <T,>(items: T[]): T[] => {
  const result = [...items];
  for (let i = result.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [result[i], result[j]] = [result[j], result[i]];
  }
  return result;
};

export const formatTime = (seconds: number): string => {
  const clamped = Math.max(0, Math.floor(seconds));
  const minutes = Math.floor(clamped / 60);
  const remaining = clamped % 60;
  return `${minutes}:${remaining.toString().padStart(2, '0')}`;
};

export type GamePhase =
  | 'START'
  | 'PLAYING'
  | 'PAUSED'
  | 'GAME_OVER'
  | 'VICTORY'
  | 'LEVEL_COMPLETE';

export interface GameState {
  status: GamePhase;
  score: number;
  level: number;
  timer?: number;
  moves?: number;
  lives?: number;
  [key: string]: any;
}

export interface MusicTrack {
  name: string;
  url: string;
  description: string;
}

export function useIsMobile(): boolean {
  const [isMobile, setIsMobile] = useState(() => {
    if (typeof window === 'undefined') return false;
    const ua =
      /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(
        navigator.userAgent,
      );
    const touch =
      window.matchMedia('(pointer: coarse)').matches ||
      'ontouchstart' in window ||
      navigator.maxTouchPoints > 0;
    const small = window.innerWidth < 768;
    return ua || touch || small;
  });

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(
        window.innerWidth < 768 ||
          window.matchMedia('(pointer: coarse)').matches ||
          'ontouchstart' in window ||
          navigator.maxTouchPoints > 0,
      );
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return isMobile;
}
