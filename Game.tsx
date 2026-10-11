import React, { useEffect, useMemo, useRef, useState } from 'react';
import type { MutableRefObject } from 'react';
import {
  getApartmentBackground,
  getCharacterSprites,
  getPoliceStationBackground,
  getStreetBackground,
  SCENE_H,
  SCENE_W,
} from './assets';
import { CASE_ONE } from './constants';
import { InputSnapshot } from './input';
import { CaseProgress, EvidenceItem, LocationId, Suspect } from './types';
import { gameAudio, GameState } from './utils';

interface Game2DProps {
  gameState: GameState;
  setGameState: React.Dispatch<React.SetStateAction<GameState>>;
  inputRef: MutableRefObject<InputSnapshot>;
  caseProgress: CaseProgress;
  setCaseProgress: React.Dispatch<React.SetStateAction<CaseProgress>>;
  onOpenEvidence: (evidence: EvidenceItem) => void;
  onOpenSuspect: (suspect: Suspect) => void;
  onLocationChange: (loc: LocationId) => void;
}

interface Obstacle {
  x: number;
  y: number;
  w: number;
  h: number;
}

export function Game({
  gameState,
  setGameState,
  inputRef,
  caseProgress,
  setCaseProgress,
  onOpenEvidence,
  onOpenSuspect,
  onLocationChange,
}: Game2DProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  // Detective Player Position (Matches Reference Entrance Spawn)
  const playerRef = useRef({
    x: 265,
    y: 510,
    dir: 'up' as 'up' | 'down' | 'left' | 'right',
    frame: 0,
    frameTimer: 0,
  });

  // Suspect / Witness in Living Room (Matches Reference)
  const witnessRef = useRef({
    x: 720,
    y: 570,
    dir: 'left' as 'up' | 'down' | 'left' | 'right',
  });

  // Detective dialogue bubble
  const [activeNarrativeQuote, setActiveNarrativeQuote] = useState(
    'يبدو أن هناك فوضى على الطاولة... ربما كان هناك شجار هنا. يجب أن أفتش كل الغرفة بدقة.',
  );
  const [isWitnessDialogOpen, setIsWitnessDialogOpen] = useState(false);

  const loc = caseProgress.currentLocation;

  // Filter evidence for current location
  const currentEvidence = useMemo(
    () => CASE_ONE.evidenceList.filter((e) => e.location === loc),
    [loc],
  );

  // Set initial spawn coordinates per location
  useEffect(() => {
    if (loc === 'apartment') {
      playerRef.current.x = 265;
      playerRef.current.y = 510;
      playerRef.current.dir = 'up';
    } else if (loc === 'station') {
      playerRef.current.x = 600;
      playerRef.current.y = 660;
      playerRef.current.dir = 'up';
    } else {
      playerRef.current.x = 600;
      playerRef.current.y = 560;
      playerRef.current.dir = 'up';
    }
  }, [loc]);

  // Exact collision boundaries matching the reference apartment layout
  const obstacles: Obstacle[] = useMemo(() => {
    if (loc === 'apartment') {
      return [
        // Outer Boundary Walls
        { x: 160, y: 20, w: 1040, h: 25 },
        { x: 160, y: 720, w: 1040, h: 25 },
        { x: 160, y: 20, w: 25, h: 700 },
        { x: 1180, y: 20, w: 25, h: 700 },

        // Internal Dividing Walls
        { x: 360, y: 260, w: 20, h: 460 }, // Entrance Wall
        { x: 160, y: 250, w: 210, h: 20 }, // Bathroom Divider
        { x: 670, y: 20, w: 20, h: 360 }, // Kitchen Divider
        { x: 430, y: 370, w: 250, h: 20 }, // Kitchen Bottom
        { x: 750, y: 380, w: 20, h: 340 }, // Study Divider
        { x: 680, y: 400, w: 500, h: 20 }, // Bedroom/Study Divider

        // Furniture colliders aligned to the illustrated environment
        { x: 755, y: 100, w: 200, h: 185 }, // Bedroom bed
        { x: 965, y: 115, w: 90, h: 45 }, // Bedroom nightstand
        { x: 445, y: 30, w: 215, h: 75 }, // Kitchen counters
        { x: 445, y: 115, w: 62, h: 95 }, // Refrigerator
        { x: 495, y: 250, w: 115, h: 78 }, // Kitchen dining table
        { x: 410, y: 475, w: 70, h: 120 }, // Living room sofa
        { x: 520, y: 650, w: 130, h: 55 }, // Bottom sofa
        { x: 510, y: 515, w: 140, h: 90 }, // Living coffee table
        { x: 835, y: 500, w: 220, h: 125 }, // Office desk
        { x: 1080, y: 440, w: 90, h: 45 }, // Office cabinet
        { x: 580, y: 730, w: 45, h: 42 }, // Balcony AC unit
      ];
    } else if (loc === 'station') {
      return [
        { x: 40, y: 40, w: 1200, h: 25 },
        { x: 40, y: 740, w: 1200, h: 25 },
        { x: 40, y: 40, w: 25, h: 720 },
        { x: 1220, y: 40, w: 25, h: 720 },
        { x: 210, y: 190, w: 140, h: 75 },
      ];
    } else {
      return [
        { x: 0, y: 0, w: 1280, h: 40 },
        { x: 0, y: 760, w: 1280, h: 40 },
        { x: 680, y: 40, w: 520, h: 240 },
        { x: 740, y: 480, w: 240, h: 120 },
      ];
    }
  }, [loc]);

  // Main 2D Render Loop
  useEffect(() => {
    let animId: number;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let lastTime = performance.now();

    const detectiveSprites = getCharacterSprites('detective');
    const witnessSprites = getCharacterSprites('neighbor');

    const render = (time: number) => {
      const dt = Math.min((time - lastTime) / 1000, 0.1);
      lastTime = time;

      const p = playerRef.current;
      const actions = inputRef.current?.actions;

      // Handle Smooth Movement
      let dx = 0;
      let dy = 0;
      if (actions?.left) dx -= 1;
      if (actions?.right) dx += 1;
      if (actions?.up) dy -= 1;
      if (actions?.down) dy += 1;

      const isRunning = actions?.run ?? false;
      const speed = isRunning ? 220 : 150;

      if (dx !== 0 && dy !== 0) {
        dx *= 0.7071;
        dy *= 0.7071;
      }

      const nextX = p.x + dx * speed * dt;
      const nextY = p.y + dy * speed * dt;

      if (dx < 0) p.dir = 'left';
      else if (dx > 0) p.dir = 'right';
      else if (dy < 0) p.dir = 'up';
      else if (dy > 0) p.dir = 'down';

      const isMoving = dx !== 0 || dy !== 0;
      if (isMoving) {
        p.frameTimer += dt * (isRunning ? 12 : 7);
        if (p.frameTimer > 1) {
          p.frame = (p.frame + 1) % 4;
          p.frameTimer = 0;
        }
      } else {
        p.frame = 0;
      }

      // Exact Collision Checking
      const pr = 14;
      let canMoveX = true;
      let canMoveY = true;

      for (let i = 0; i < obstacles.length; i++) {
        const obs = obstacles[i];
        if (
          nextX + pr > obs.x &&
          nextX - pr < obs.x + obs.w &&
          p.y + pr > obs.y &&
          p.y - pr < obs.y + obs.h
        ) {
          canMoveX = false;
        }
        if (
          p.x + pr > obs.x &&
          p.x - pr < obs.x + obs.w &&
          nextY + pr > obs.y &&
          nextY - pr < obs.y + obs.h
        ) {
          canMoveY = false;
        }
      }

      const W = SCENE_W;
      const H = SCENE_H;

      if (canMoveX) p.x = Math.max(30, Math.min(W - 30, nextX));
      if (canMoveY) p.y = Math.max(40, Math.min(H - 40, nextY));

      // 1. Draw Layered Pre-rendered Apartment / Station Background
      ctx.clearRect(0, 0, W, H);
      if (loc === 'apartment') {
        const bg = getApartmentBackground();
        ctx.drawImage(bg, 0, 0, W, H);
      } else if (loc === 'station') {
        const bg = getPoliceStationBackground();
        ctx.drawImage(bg, 0, 0, W, H);
      } else {
        const bg = getStreetBackground();
        ctx.drawImage(bg, 0, 0, W, H);
      }

      // 2. Render Interactive Evidence Markers (Matches Exact Reference Coordinates)
      let activeEvidence: EvidenceItem | null = null;
      let minEvidenceDist = 999;

      for (let i = 0; i < currentEvidence.length; i++) {
        const ev = currentEvidence[i];
        const [ex, ey] = getReferenceEvidenceCoords(loc, ev.id);
        const dist = Math.hypot(p.x - ex, p.y - ey);
        const isDiscovered = caseProgress.discoveredEvidenceIds.includes(ev.id);

        if (dist < minEvidenceDist) {
          minEvidenceDist = dist;
          if (dist < 55) {
            activeEvidence = ev;
          }
        }

        // Draw Reference Golden Glowing '?' Disc or Checkmark
        drawReferenceGlowingQuestionMark(ctx, ex, ey, isDiscovered, dist < 55, time);
      }

      // 3. Render NPCs with Reference Dialogue Bubble
      if (loc === 'apartment') {
        drawCharacterSpriteOnLayer(ctx, witnessSprites, witnessRef.current.x, witnessRef.current.y, witnessRef.current.dir, 0, false);
        drawReferenceSpeechBubbleIcon(ctx, witnessRef.current.x, witnessRef.current.y - 48, time);
      }

      // 4. Render Detective Sprite (Layered Character)
      drawCharacterSpriteOnLayer(ctx, detectiveSprites, p.x, p.y, p.dir, p.frame, isMoving);

      // The illustrated background already contains baked cinematic lighting.
      // Avoid overlaying blurry circular gradients that obscure the artwork.

      // 6. Action Prompt Triggers
      const distToWitness = Math.hypot(p.x - witnessRef.current.x, p.y - witnessRef.current.y);
      const nearWitness = loc === 'apartment' && distToWitness < 65;

      if (activeEvidence) {
        if (actions?.interact || actions?.confirm) {
          actions.interact = false;
          actions.confirm = false;
          gameAudio.playConfirmSelect();
          onOpenEvidence(activeEvidence);
        }
      } else if (nearWitness) {
        if (actions?.interact || actions?.confirm) {
          actions.interact = false;
          actions.confirm = false;
          gameAudio.playConfirmSelect();
          setIsWitnessDialogOpen(true);
        }
      }

      animId = requestAnimationFrame(render);
    };

    animId = requestAnimationFrame(render);
    return () => cancelAnimationFrame(animId);
  }, [
    loc,
    currentEvidence,
    caseProgress.discoveredEvidenceIds,
    obstacles,
    onOpenEvidence,
    onOpenSuspect,
  ]);

  return (
    <div className="w-full h-full flex flex-col items-center justify-center bg-zinc-950 relative overflow-hidden select-none">
      {/* 2D Hand-Painted Canvas */}
      <canvas
        ref={canvasRef}
        width={SCENE_W}
        height={SCENE_H}
        className="w-full h-full object-contain max-w-7xl shadow-2xl rounded-2xl border border-amber-500/20 bg-zinc-950"
      />

      {/* TOP-RIGHT QUEST OBJECTIVES HUD (IDENTICAL TO REFERENCE IMAGE) */}
      <div className="absolute top-4 right-4 z-20 pointer-events-none hidden sm:flex flex-col gap-2">
        <div className="bg-zinc-950/90 border border-zinc-700/60 rounded-xl p-3.5 shadow-2xl backdrop-blur-md text-right w-64">
          <div className="flex items-center justify-between border-b border-zinc-800 pb-2 mb-2">
            <span className="text-amber-400 font-bold text-xs flex items-center gap-1.5">
              <span>📋</span> القضية الحالية
            </span>
            <span className="text-xs text-amber-300 font-bold">الساعة اللي وقفت</span>
          </div>

          <div className="flex flex-col gap-2 text-xs">
            <div className="flex items-center gap-2 text-emerald-400 font-medium">
              <span className="w-4 h-4 rounded border border-emerald-500 bg-emerald-500/20 flex items-center justify-center text-[10px]">
                ✓
              </span>
              <span className="text-zinc-200">تفتيش الشقة</span>
            </div>

            <div className="flex items-center gap-2 text-zinc-400">
              <span className="w-4 h-4 rounded border border-zinc-700 bg-zinc-900/60 flex items-center justify-center text-[10px]"></span>
              <span>التحدث مع الجيران</span>
            </div>

            <div className="flex items-center gap-2 text-zinc-400">
              <span className="w-4 h-4 rounded border border-zinc-700 bg-zinc-900/60 flex items-center justify-center text-[10px]"></span>
              <span>جمع الأدلة ({caseProgress.discoveredEvidenceIds.length}/6)</span>
            </div>

            <div className="flex items-center gap-2 text-zinc-400">
              <span className="w-4 h-4 rounded border border-zinc-700 bg-zinc-900/60 flex items-center justify-center text-[10px]"></span>
              <span>مراجعة كاميرات العمارة</span>
            </div>

            <div className="flex items-center gap-2 text-zinc-400">
              <span className="w-4 h-4 rounded border border-zinc-700 bg-zinc-900/60 flex items-center justify-center text-[10px]"></span>
              <span>تقرير للقسم</span>
            </div>
          </div>
        </div>
      </div>

      {/* TOP-LEFT LOCATION BADGE (MATCHING REFERENCE) */}
      <div className="absolute top-4 left-4 z-20 pointer-events-none">
        <div className="bg-zinc-950/90 border border-zinc-700/60 rounded-xl px-4 py-2 shadow-xl backdrop-blur-md text-amber-300 text-xs font-bold flex items-center gap-2">
          <span>☀️ 11:15 PM</span>
          <span className="text-zinc-600">|</span>
          <span>شقة - مدينة نصر</span>
        </div>
      </div>

      {/* BOTTOM-LEFT DETECTIVE NARRATIVE BOX (EXACT MATCH TO REFERENCE IMAGE) */}
      <div className="absolute bottom-6 left-6 z-30 max-w-lg hidden sm:flex items-center gap-4 bg-zinc-950/95 border border-zinc-700/80 rounded-2xl p-3.5 shadow-2xl backdrop-blur-md text-right">
        <div className="w-16 h-16 rounded-xl bg-gradient-to-tr from-stone-900 via-amber-950 to-slate-800 border-2 border-amber-500/60 flex items-center justify-center text-3xl shadow-inner shrink-0 relative overflow-hidden">
          <span className="relative z-10">🕵️‍♂️</span>
        </div>
        <div className="flex-1 text-right">
          <h4 className="text-amber-400 font-bold text-sm mb-1">يوسف المصري</h4>
          <p className="text-xs text-zinc-300 leading-relaxed">{activeNarrativeQuote}</p>
        </div>
      </div>

      {/* CENTER-BOTTOM INTERACTION PROMPT "اضغط للفحص E" */}
      <div className="absolute bottom-6 left-1/2 -translate-x-1/2 z-30 pointer-events-none">
        <div className="bg-zinc-950/90 border border-amber-500/50 rounded-xl px-4 py-2 shadow-xl backdrop-blur-md flex items-center gap-2">
          <span className="px-2 py-0.5 bg-amber-500 text-zinc-950 font-bold text-xs rounded">E</span>
          <span className="text-amber-200 text-xs font-bold">اضغط للفحص</span>
        </div>
      </div>

      {/* BOTTOM-RIGHT QUICK ACTION TOOLS (BAG, NOTEBOOK, MAP) */}
      <div className="absolute bottom-6 right-6 z-30 flex flex-col gap-3">
        <button
          onClick={() => {
            gameAudio.playCrispClick();
            onLocationChange(loc === 'apartment' ? 'station' : 'apartment');
          }}
          className="w-12 h-12 rounded-full bg-zinc-950/90 border border-amber-500/50 hover:border-amber-400 flex flex-col items-center justify-center text-amber-300 shadow-xl transition-all active:scale-95 group">
          <span className="text-lg">📍</span>
          <span className="text-[9px] text-zinc-400 font-bold group-hover:text-amber-300">الخريطة</span>
        </button>

        <button
          onClick={() => {
            gameAudio.playCrispClick();
            setGameState((prev) => ({ ...prev, status: 'PLAYING' }));
          }}
          className="w-12 h-12 rounded-full bg-zinc-950/90 border border-amber-500/50 hover:border-amber-400 flex flex-col items-center justify-center text-amber-300 shadow-xl transition-all active:scale-95 group">
          <span className="text-lg">📒</span>
          <span className="text-[9px] text-zinc-400 font-bold group-hover:text-amber-300">دفتر القضية</span>
        </button>
      </div>

      {/* WITNESS DIALOGUE MODAL */}
      {isWitnessDialogOpen && (
        <div className="fixed inset-x-4 bottom-6 max-w-2xl mx-auto z-50 bg-zinc-950/95 border-2 border-amber-500/60 rounded-2xl p-4 shadow-2xl backdrop-blur-md text-right animate-in fade-in slide-in-from-bottom-4 duration-200">
          <div className="flex items-center gap-3 border-b border-zinc-800 pb-2 mb-3">
            <div className="w-12 h-12 rounded-xl bg-amber-900/40 border border-amber-500/50 flex items-center justify-center text-2xl">
              🧔
            </div>
            <div>
              <h3 className="font-bold text-amber-400 text-sm">عمرو سعيد (الجار المقابل)</h3>
              <span className="text-[11px] text-zinc-400">شاهد عيان</span>
            </div>
          </div>

          <p className="text-xs text-zinc-200 leading-relaxed mb-4">
            "يا فندم أنا سمعت صوت زعيق جامد وتكسير بعد الساعة 10 بالليل.. وبعدها بدقائق شفت عربية سودا بتتحرك بسرعة من قدام العمارة!"
          </p>

          <button
            onClick={() => {
              gameAudio.playCrispClick();
              setIsWitnessDialogOpen(false);
            }}
            className="w-full py-2 bg-amber-500 hover:bg-amber-400 text-zinc-950 font-bold rounded-lg text-xs transition-all">
            تم الاستماع للشاهد (إغلاق) ↵
          </button>
        </div>
      )}
    </div>
  );
}

// ====================================================================
// EXACT REFERENCE ART COORDINATE MAPPING & LIGHTING HELPERS
// ====================================================================

function getReferenceEvidenceCoords(loc: LocationId, id: string): [number, number] {
  if (loc === 'apartment') {
    switch (id) {
      case 'clock':
        // Living Room Center Coffee Table
        return [930, 88];
      case 'tea_cup':
        // Kitchen Dining Table
        return [550, 286];
      case 'missing_cable':
        // Bedroom Bed Area
        return [840, 230];
      case 'desk_contract':
        // Office Executive Desk Laptop Area
        return [945, 560];
      case 'bank_receipt':
        // Bedroom Vanity Desk
        return [990, 235];
      case 'damaged_vase':
        // Broken vase by the apartment entrance
        return [310, 455];
      case 'muddy_footprint':
        // Muddy footprint on the balcony
        return [600, 750];
      default:
        return [545, 555];
    }
  } else if (loc === 'station') {
    return [280, 220];
  } else {
    return [940, 160];
  }
}

function drawCharacterSpriteOnLayer(
  ctx: CanvasRenderingContext2D,
  sprites: any,
  x: number,
  y: number,
  dir: 'down' | 'up' | 'left' | 'right',
  frame: number,
  isMoving: boolean,
) {
  const dirSprites = sprites[dir] || sprites.down;
  const spriteCanvas = isMoving ? dirSprites.walk[frame % dirSprites.walk.length] : dirSprites.idle;

  if (spriteCanvas) {
    ctx.drawImage(spriteCanvas, x - 24, y - 50);
  }
}

function drawReferenceGlowingQuestionMark(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  isDiscovered: boolean,
  isNear: boolean,
  time: number,
) {
  const hover = Math.sin(time * 0.006) * 3;
  const my = y + hover;

  if (isDiscovered) {
    // Green Checkmark
    ctx.fillStyle = '#10b981';
    ctx.beginPath();
    ctx.arc(x, my, 11, 0, Math.PI * 2);
    ctx.fill();
    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 12px sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText('✓', x, my + 4);
    return;
  }

  // Exact Match to Reference Image Glowing Yellow Question Mark
  ctx.save();
  // Radial Amber Halo
  const halo = ctx.createRadialGradient(x, my, 2, x, my, 20 + Math.sin(time * 0.008) * 3);
  halo.addColorStop(0, 'rgba(245, 158, 11, 0.7)');
  halo.addColorStop(0.6, 'rgba(217, 119, 6, 0.2)');
  halo.addColorStop(1, 'rgba(0, 0, 0, 0)');
  ctx.fillStyle = halo;
  ctx.beginPath();
  ctx.arc(x, my, 20, 0, Math.PI * 2);
  ctx.fill();

  // Yellow Circle Badge
  ctx.fillStyle = '#f59e0b';
  ctx.beginPath();
  ctx.arc(x, my, 12, 0, Math.PI * 2);
  ctx.fill();
  ctx.strokeStyle = '#fef08a';
  ctx.lineWidth = 1.5;
  ctx.stroke();

  // Dark Question Symbol
  ctx.fillStyle = '#0f172a';
  ctx.font = 'bold 14px sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText('?', x, my + 5);

  if (isNear) {
    // Outer Ring
    ctx.strokeStyle = '#fbbf24';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.arc(x, my, 16, 0, Math.PI * 2);
    ctx.stroke();
  }

  ctx.restore();
}

function drawReferenceSpeechBubbleIcon(ctx: CanvasRenderingContext2D, x: number, y: number, time: number) {
  const hover = Math.sin(time * 0.007) * 3;
  const my = y + hover;

  ctx.fillStyle = '#ffffff';
  ctx.beginPath();
  ctx.roundRect(x - 12, my - 10, 24, 18, 6);
  ctx.fill();

  // Speech tail
  ctx.beginPath();
  ctx.moveTo(x - 3, my + 8);
  ctx.lineTo(x, my + 13);
  ctx.lineTo(x + 4, my + 8);
  ctx.fill();

  // Three dots
  ctx.fillStyle = '#0f172a';
  ctx.beginPath();
  ctx.arc(x - 6, my - 1, 1.5, 0, Math.PI * 2);
  ctx.arc(x, my - 1, 1.5, 0, Math.PI * 2);
  ctx.arc(x + 6, my - 1, 1.5, 0, Math.PI * 2);
  ctx.fill();
}

function drawReferenceWarmLightingLayer(
  ctx: CanvasRenderingContext2D,
  W: number,
  H: number,
  loc: LocationId,
  time: number,
) {
  if (loc !== 'apartment') return;

  // Exact Lamp positions from reference image
  const warmLights = [
    { x: 190, y: 350, r: 120 }, // Foyer Lamp L
    { x: 340, y: 620, r: 120 }, // Foyer Lamp R
    { x: 255, y: 280, r: 90 }, // Above Door 42 Sconce
    { x: 605, y: 115, r: 130 }, // Kitchen Lamp
    { x: 810, y: 110, r: 130 }, // Bedroom Lamp
    { x: 885, y: 340, r: 100 }, // Bedroom Vanity Lamp
    { x: 650, y: 460, r: 140 }, // Living Room TV Lamp
    { x: 395, y: 605, r: 130 }, // Living Room Sofa Lamp
    { x: 865, y: 600, r: 110 }, // Office Lamp
    { x: 510, y: 770, r: 90 }, // Balcony Lantern
  ];

  ctx.save();
  ctx.globalCompositeOperation = 'screen';

  for (let i = 0; i < warmLights.length; i++) {
    const l = warmLights[i];
    const rad = ctx.createRadialGradient(l.x, l.y, 4, l.x, l.y, l.r);
    rad.addColorStop(0, 'rgba(251, 191, 36, 0.45)');
    rad.addColorStop(0.5, 'rgba(245, 158, 11, 0.16)');
    rad.addColorStop(1, 'rgba(0, 0, 0, 0)');
    ctx.fillStyle = rad;
    ctx.beginPath();
    ctx.arc(l.x, l.y, l.r, 0, Math.PI * 2);
    ctx.fill();
  }

  ctx.restore();
}
