// tslint:disable
/* eslint-disable */
import {
  BookOpen,
  CheckCircle,
  Clock,
  Compass,
  FileSearch,
  FileText,
  HelpCircle,
  Lightbulb,
  MapPin,
  Menu,
  Play,
  RotateCw,
  Search,
  Shield,
  Star,
  Users,
  Volume2,
  VolumeX,
  X,
  Zap,
} from 'lucide-react';
import React, {useCallback, useEffect, useRef, useState} from 'react';
import {CASE_ONE, getConfig} from './constants';
import {Game} from './Game';
import {InputSnapshot, useInput} from './input';
import {CaseProgress, EvidenceItem, LocationId, Suspect} from './types';
import BackgroundMusic from './utils/BackgroundMusic';
import {
  gameAudio,
  GameState,
  MenuOverlay,
  Overlay,
  useAutoFocus,
  useIsMobile,
} from './utils/helper';
import {
  usePlayground,
  usePlaygroundGameLifecycle,
} from './utils/usePlayground';

const STORAGE_KEY = 'maftuh_detective_save_v1';

export default function App() {
  const {sdk, isReady} = usePlayground();
  const isMobile = useIsMobile();

  const [gameState, setGameState] = useState<GameState>({
    status: 'START',
    score: 0,
    level: 1,
  });

  const [isMusicMuted, setIsMusicMuted] = useState(false);
  const [isSfxMuted, setIsSfxMuted] = useState(false);
  const [isGameMenuOpen, setIsGameMenuOpen] = useState(false);
  const [isHowToPlayOpen, setIsHowToPlayOpen] = useState(false);

  // Case Progress State
  const [caseProgress, setCaseProgress] = useState<CaseProgress>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) {
        return JSON.parse(saved);
      }
    } catch (_) {}
    return {
      discoveredEvidenceIds: [],
      interrogatedSuspectIds: [],
      brokenAlibiSuspectIds: [],
      currentLocation: 'apartment',
      notes: [],
      flashlightOn: false,
      inspectedEvidenceId: null,
      activeSuspectId: null,
      activeDialogueNodeId: null,
      evidenceBoardLinks: [],
      verdictSubmitted: false,
      completedCases: [],
    };
  });

  // Active UI Views
  const [activeTab, setActiveTab] = useState<'none' | 'notebook' | 'board' | 'map' | 'accuse'>('none');
  const [selectedEvidenceForInspection, setSelectedEvidenceForInspection] = useState<EvidenceItem | null>(null);
  const [selectedSuspectForInterrogation, setSelectedSuspectForInterrogation] = useState<Suspect | null>(null);
  const [currentDialogueNodeId, setCurrentDialogueNodeId] = useState<string | null>(null);
  const [dialogueLog, setDialogueLog] = useState<{speaker: string; text: string; isAlert?: boolean}[]>([]);

  // Accusation State
  const [accusedSuspectId, setAccusedSuspectId] = useState<string>('sherif');
  const [accusedMotive, setAccusedMotive] = useState<string>('embezzlement');
  const [selectedProofIds, setSelectedProofIds] = useState<string[]>([]);
  const [verdictResult, setVerdictResult] = useState<'perfect' | 'partial' | 'wrong' | null>(null);

  // Input Hook
  const canvasWrapperRef = useRef<HTMLDivElement>(null);
  const inputRef = useInput(canvasWrapperRef, {enabled: gameState.status === 'PLAYING' && activeTab === 'none' && !selectedEvidenceForInspection && !selectedSuspectForInterrogation && !isGameMenuOpen && !isHowToPlayOpen});

  // Mobile virtual joystick state
  const joystickTouchRef = useRef<{startX: number; startY: number; active: boolean}>({
    startX: 0,
    startY: 0,
    active: false,
  });

  // Save to LocalStorage
  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(caseProgress));
    } catch (_) {}
  }, [caseProgress]);

  // Sync SDK Lifecycle
  usePlaygroundGameLifecycle(gameState, sdk);

  // Keyboard navigation for Escape menu
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        if (selectedEvidenceForInspection || selectedSuspectForInterrogation || activeTab !== 'none') {
          setSelectedEvidenceForInspection(null);
          setSelectedSuspectForInterrogation(null);
          setActiveTab('none');
        } else if (gameState.status === 'PLAYING') {
          setIsGameMenuOpen((prev) => !prev);
        }
      } else if (e.key === 'Tab' || e.key === 'n' || e.key === 'N') {
        if (gameState.status === 'PLAYING') {
          setActiveTab((prev) => (prev === 'notebook' ? 'none' : 'notebook'));
        }
      } else if (e.key === 'f' || e.key === 'F') {
        if (gameState.status === 'PLAYING') {
          setCaseProgress((prev) => ({...prev, flashlightOn: !prev.flashlightOn}));
        }
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [gameState.status, selectedEvidenceForInspection, selectedSuspectForInterrogation, activeTab]);

  // Start Game
  const initGame = useCallback(() => {
    setGameState({
      status: 'PLAYING',
      score: caseProgress.discoveredEvidenceIds.length * 50 + caseProgress.brokenAlibiSuspectIds.length * 100,
      level: 1,
    });
    setIsGameMenuOpen(false);
    setIsHowToPlayOpen(false);
    if (sdk) {
      sdk.startGame();
    }
  }, [sdk, caseProgress]);

  // Evidence Discovery Handler
  const handleOpenEvidence = (ev: EvidenceItem) => {
    setSelectedEvidenceForInspection(ev);
    if (!caseProgress.discoveredEvidenceIds.includes(ev.id)) {
      gameAudio.playPowerupChime();
      setCaseProgress((prev) => ({
        ...prev,
        discoveredEvidenceIds: [...prev.discoveredEvidenceIds, ev.id],
      }));
      setGameState((prev) => ({
        ...prev,
        score: prev.score + 50,
      }));
    }
  };

  // Suspect Interrogation Opener
  const handleOpenSuspect = (sus: Suspect) => {
    setSelectedSuspectForInterrogation(sus);
    const rootNode = sus.dialogueRootNodeId;
    setCurrentDialogueNodeId(rootNode);
    setDialogueLog([
      {
        speaker: sus.nameAr,
        text: sus.dialogueNodes[rootNode]?.suspectLineAr || 'أهلاً يا حضرة الضابط.',
      },
    ]);
  };

  // Handle Dialogue Option Click
  const handleSelectDialogueOption = (opt: any) => {
    if (!selectedSuspectForInterrogation) return;

    gameAudio.playCrispClick();

    const newLog = [
      ...dialogueLog,
      {
        speaker: 'النقيب يوسف',
        text: opt.textAr,
      },
      {
        speaker: selectedSuspectForInterrogation.nameAr,
        text: opt.responseAr,
      },
    ];

    if (opt.breaksAlibi && !caseProgress.brokenAlibiSuspectIds.includes(selectedSuspectForInterrogation.id)) {
      gameAudio.playPowerupChime();
      newLog.push({
        speaker: 'إنجاز تحقيقي',
        text: opt.alibiBrokenTextAr || 'تم كسر حجة غياب المشتبه به وإثبات تناقض أقواله!',
        isAlert: true,
      });

      setCaseProgress((prev) => ({
        ...prev,
        brokenAlibiSuspectIds: [...prev.brokenAlibiSuspectIds, selectedSuspectForInterrogation.id],
      }));

      setGameState((prev) => ({
        ...prev,
        score: prev.score + 150,
      }));
    }

    setDialogueLog(newLog);
  };

  // Handle Location Travel
  const handleLocationChange = (loc: LocationId) => {
    gameAudio.playConfirmSelect();
    setCaseProgress((prev) => ({...prev, currentLocation: loc}));
    setActiveTab('none');
  };

  // Accusation Execution
  const handleExecuteAccusation = () => {
    const isCorrectSuspect = accusedSuspectId === CASE_ONE.correctCulpritId;
    const hasEnoughProof = selectedProofIds.length >= 2 && selectedProofIds.includes('cctv_footage');

    let result: 'perfect' | 'partial' | 'wrong' = 'wrong';

    if (isCorrectSuspect && hasEnoughProof) {
      result = 'perfect';
      gameAudio.playVictoryFanfare();
      setGameState((prev) => ({
        ...prev,
        score: prev.score + 500,
        status: 'VICTORY',
      }));
    } else if (isCorrectSuspect && !hasEnoughProof) {
      result = 'partial';
      gameAudio.playConfirmSelect();
      setGameState((prev) => ({
        ...prev,
        score: prev.score + 200,
        status: 'VICTORY',
      }));
    } else {
      result = 'wrong';
      gameAudio.playDefeatMotif();
      setGameState((prev) => ({
        ...prev,
        status: 'GAME_OVER',
      }));
    }

    setVerdictResult(result);
    setCaseProgress((prev) => ({
      ...prev,
      verdictSubmitted: true,
      verdictResult: result,
      completedCases: [...prev.completedCases, CASE_ONE.id],
    }));
  };

  // Virtual Joystick Pointer Handlers
  const handleJoystickPointerDown = (e: React.PointerEvent) => {
    e.currentTarget.setPointerCapture(e.pointerId);
    joystickTouchRef.current = {
      startX: e.clientX,
      startY: e.clientY,
      active: true,
    };
  };

  const handleJoystickPointerMove = (e: React.PointerEvent) => {
    if (!joystickTouchRef.current.active) return;
    const dx = e.clientX - joystickTouchRef.current.startX;
    const dy = e.clientY - joystickTouchRef.current.startY;
    const threshold = 15;

    inputRef.current.actions.left = dx < -threshold;
    inputRef.current.actions.right = dx > threshold;
    inputRef.current.actions.up = dy < -threshold;
    inputRef.current.actions.down = dy > threshold;
  };

  const handleJoystickPointerUp = () => {
    joystickTouchRef.current.active = false;
    inputRef.current.actions.left = false;
    inputRef.current.actions.right = false;
    inputRef.current.actions.up = false;
    inputRef.current.actions.down = false;
  };

  const startBtnRef = useAutoFocus<HTMLButtonElement>(gameState.status === 'START');

  return (
    <div
      dir="rtl"
      className="fixed inset-0 overflow-hidden touch-none flex flex-col bg-zinc-950 text-amber-50 font-sans select-none">
      <BackgroundMusic
        url={getConfig('backgroundMusicUrl')}
        isPlaying={gameState.status === 'PLAYING'}
        isMuted={isMusicMuted}
        volume={getConfig('defaultMusicVolume')}
      />

      {/* Top HUD Safe-Zone Bar */}
      {gameState.status === 'PLAYING' && (
        <header className="w-full bg-zinc-900/90 border-b border-amber-500/20 px-3 py-2 flex items-center justify-between z-30 shrink-0 backdrop-blur-md">
          {/* Right Side: Menu Button Reserved strictly at corner */}
          <div className="flex items-center gap-2">
            <button
              onClick={() => {
                gameAudio.playCrispClick();
                setIsGameMenuOpen(true);
              }}
              className="px-3 py-1.5 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/40 rounded-lg text-amber-400 text-xs font-bold flex items-center gap-1.5 transition-all">
              <Menu size={16} />
              <span>القائمة</span>
            </button>
            <div className="hidden sm:flex items-center gap-1 text-xs text-zinc-400 bg-zinc-800/60 px-2.5 py-1 rounded-md">
              <MapPin size={13} className="text-amber-400" />
              <span>
                {CASE_ONE.locations.find((l) => l.id === caseProgress.currentLocation)?.nameAr}
              </span>
            </div>
          </div>

          {/* Center Title & Stats */}
          <div className="flex items-center gap-4 text-xs font-semibold">
            <div className="flex items-center gap-1.5 text-amber-400 bg-amber-950/40 px-2.5 py-1 rounded-md border border-amber-500/30">
              <Search size={14} />
              <span>الأدلة: {caseProgress.discoveredEvidenceIds.length}/8</span>
            </div>
            <div className="flex items-center gap-1.5 text-emerald-400 bg-emerald-950/40 px-2.5 py-1 rounded-md border border-emerald-500/30">
              <Zap size={14} />
              <span>النقاط: {gameState.score}</span>
            </div>
          </div>

          {/* Left Side Quick Detective Tools */}
          <div className="flex items-center gap-2">
            <button
              onClick={() => {
                gameAudio.playConfirmSelect();
                setActiveTab((prev) => (prev === 'notebook' ? 'none' : 'notebook'));
              }}
              className={`px-3 py-1.5 rounded-lg border text-xs font-bold transition-all flex items-center gap-1.5 ${
                activeTab === 'notebook'
                  ? 'bg-amber-500 text-zinc-950 border-amber-400 font-bold shadow-md'
                  : 'bg-zinc-800 text-amber-400 border-zinc-700 hover:bg-zinc-700'
              }`}>
              <BookOpen size={16} />
              <span>دفتر التحقيق</span>
            </button>

            <button
              onClick={() => {
                gameAudio.playConfirmSelect();
                setActiveTab((prev) => (prev === 'map' ? 'none' : 'map'));
              }}
              className={`px-3 py-1.5 rounded-lg border text-xs font-bold transition-all flex items-center gap-1.5 ${
                activeTab === 'map'
                  ? 'bg-amber-500 text-zinc-950 border-amber-400 font-bold shadow-md'
                  : 'bg-zinc-800 text-amber-400 border-zinc-700 hover:bg-zinc-700'
              }`}>
              <Compass size={16} />
              <span>الخريطة</span>
            </button>

            <button
              onClick={() => {
                gameAudio.playConfirmSelect();
                setActiveTab((prev) => (prev === 'accuse' ? 'none' : 'accuse'));
              }}
              className="px-3 py-1.5 bg-red-900/80 hover:bg-red-800 border border-red-500 text-red-200 text-xs font-bold rounded-lg flex items-center gap-1.5 transition-all shadow-md">
              <Shield size={14} />
              <span>توجيه الاتهام</span>
            </button>
          </div>
        </header>
      )}

      {/* 3D Canvas Area */}
      <div ref={canvasWrapperRef} className="flex-1 w-full min-h-0 relative">
        <Game
          gameState={gameState}
          setGameState={setGameState}
          inputRef={inputRef}
          caseProgress={caseProgress}
          setCaseProgress={setCaseProgress}
          onOpenEvidence={handleOpenEvidence}
          onOpenSuspect={handleOpenSuspect}
          onLocationChange={handleLocationChange}
        />

        {/* Interactive Prompt Notification when close to clues */}
        {gameState.status === 'PLAYING' && activeTab === 'none' && !selectedEvidenceForInspection && !selectedSuspectForInterrogation && (
          <div className="absolute bottom-20 left-1/2 -translate-x-1/2 pointer-events-none z-20 flex flex-col items-center gap-1">
            <div className="bg-zinc-950/85 border border-amber-500/40 text-amber-300 text-xs md:text-sm px-4 py-1.5 rounded-full shadow-xl backdrop-blur-md animate-pulse">
              اضغط <kbd className="bg-amber-500 text-zinc-950 px-1.5 py-0.5 rounded font-bold mx-1">E</kbd> أو زر التفاعل للفحص / الاستجواب
            </div>
          </div>
        )}

        {/* Mobile Virtual Controls */}
        {isMobile && gameState.status === 'PLAYING' && activeTab === 'none' && !selectedEvidenceForInspection && !selectedSuspectForInterrogation && (
          <div className="absolute inset-x-0 bottom-3 px-4 flex justify-between items-end pointer-events-none z-20">
            {/* Left Joystick Area */}
            <div
              onPointerDown={handleJoystickPointerDown}
              onPointerMove={handleJoystickPointerMove}
              onPointerUp={handleJoystickPointerUp}
              onPointerCancel={handleJoystickPointerUp}
              className="w-28 h-28 rounded-full bg-zinc-900/60 border-2 border-amber-500/30 flex items-center justify-center pointer-events-auto touch-none active:bg-zinc-800/80 shadow-2xl backdrop-blur-sm">
              <div className="w-12 h-12 rounded-full bg-amber-500/40 border border-amber-400" />
            </div>

            {/* Right Action Buttons */}
            <div className="flex flex-col gap-2.5 pointer-events-auto">
              <div className="flex gap-2">
                <button
                  onPointerDown={() => {
                    inputRef.current.actions.flashlight = true;
                    setCaseProgress((prev) => ({...prev, flashlightOn: !prev.flashlightOn}));
                    gameAudio.playCrispClick();
                  }}
                  className={`w-12 h-12 rounded-full flex items-center justify-center border text-xs font-bold transition-all shadow-lg ${
                    caseProgress.flashlightOn
                      ? 'bg-amber-500 text-zinc-950 border-amber-300'
                      : 'bg-zinc-900/80 text-amber-400 border-amber-500/30'
                  }`}>
                  <Lightbulb size={20} />
                </button>

                <button
                  onPointerDown={() => {
                    inputRef.current.actions.run = true;
                  }}
                  onPointerUp={() => {
                    inputRef.current.actions.run = false;
                  }}
                  className="w-12 h-12 rounded-full bg-zinc-900/80 border border-amber-500/30 text-amber-400 flex items-center justify-center active:bg-amber-500 active:text-zinc-950 shadow-lg">
                  <Zap size={20} />
                </button>
              </div>

              <button
                onPointerDown={() => {
                  inputRef.current.actions.interact = true;
                  setTimeout(() => {
                    inputRef.current.actions.interact = false;
                  }, 200);
                }}
                className="w-26 h-12 bg-amber-500 text-zinc-950 font-black rounded-xl border border-amber-300 shadow-xl flex items-center justify-center gap-1.5 active:scale-95">
                <Search size={18} />
                <span className="text-xs">تفاعل / فحص</span>
              </button>
            </div>
          </div>
        )}
      </div>

      {/* CLUE INSPECTION MODAL */}
      {selectedEvidenceForInspection && (
        <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-center justify-center p-4">
          <div className="w-full max-w-lg bg-zinc-900 border-2 border-amber-500/50 rounded-2xl p-6 shadow-2xl flex flex-col gap-4 text-right animate-in fade-in zoom-in-95 duration-200">
            <div className="flex items-center justify-between border-b border-zinc-800 pb-3">
              <div className="flex items-center gap-2">
                <span className="p-2 bg-amber-500/20 text-amber-400 rounded-lg">
                  <FileSearch size={22} />
                </span>
                <div>
                  <h2 className="text-lg font-bold text-amber-400">
                    {selectedEvidenceForInspection.titleAr}
                  </h2>
                  <span className="text-xs text-zinc-400">
                    {selectedEvidenceForInspection.categoryAr} · {selectedEvidenceForInspection.locationAr}
                  </span>
                </div>
              </div>
              <button
                onClick={() => setSelectedEvidenceForInspection(null)}
                className="p-1.5 text-zinc-400 hover:text-white bg-zinc-800 rounded-lg">
                <X size={18} />
              </button>
            </div>

            <div className="bg-zinc-950/70 p-3.5 rounded-xl border border-zinc-800 text-sm leading-relaxed text-zinc-200">
              <p className="font-medium text-amber-200/90 mb-1.5">الوصف الميداني:</p>
              <p>{selectedEvidenceForInspection.description}</p>
            </div>

            <div className="bg-amber-950/30 p-3.5 rounded-xl border border-amber-500/30 text-sm leading-relaxed text-amber-100">
              <p className="font-bold text-amber-400 flex items-center gap-1.5 mb-1">
                <Shield size={16} />
                تقرير الفحص والتحليل الجنائي:
              </p>
              <p>{selectedEvidenceForInspection.forensicDetail}</p>
            </div>

            <button
              onClick={() => {
                gameAudio.playCrispClick();
                setSelectedEvidenceForInspection(null);
              }}
              className="w-full py-2.5 bg-amber-500 text-zinc-950 font-bold rounded-xl hover:bg-amber-400 transition-all text-sm">
              إضافة لدفتر التحقيق والعودة
            </button>
          </div>
        </div>
      )}

      {/* SUSPECT INTERROGATION MODAL */}
      {selectedSuspectForInterrogation && (
        <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-center justify-center p-4">
          <div className="w-full max-w-xl bg-zinc-900 border-2 border-amber-500/50 rounded-2xl p-5 md:p-6 shadow-2xl flex flex-col gap-4 text-right max-h-[90vh] overflow-hidden">
            {/* Header */}
            <div className="flex items-center justify-between border-b border-zinc-800 pb-3">
              <div className="flex items-center gap-3">
                <div
                  className="w-10 h-10 rounded-full flex items-center justify-center font-black text-white text-base shadow-md"
                  style={{backgroundColor: selectedSuspectForInterrogation.avatarColor}}>
                  {selectedSuspectForInterrogation.nameAr.charAt(0)}
                </div>
                <div>
                  <h2 className="text-base md:text-lg font-bold text-amber-400">
                    استجواب: {selectedSuspectForInterrogation.nameAr} ({selectedSuspectForInterrogation.age} سنة)
                  </h2>
                  <span className="text-xs text-zinc-400">
                    {selectedSuspectForInterrogation.roleAr}
                  </span>
                </div>
              </div>
              <button
                onClick={() => setSelectedSuspectForInterrogation(null)}
                className="p-1.5 text-zinc-400 hover:text-white bg-zinc-800 rounded-lg">
                <X size={18} />
              </button>
            </div>

            {/* Suspect Stated Alibi */}
            <div className="bg-zinc-950/70 p-3 rounded-xl border border-zinc-800 text-xs md:text-sm">
              <span className="text-amber-400 font-bold block mb-1">حجة الغياب المسجلة:</span>
              <p className="text-zinc-300 italic">"{selectedSuspectForInterrogation.initialAlibiAr}"</p>
              {caseProgress.brokenAlibiSuspectIds.includes(selectedSuspectForInterrogation.id) && (
                <span className="mt-2 inline-flex items-center gap-1 text-red-400 font-bold text-xs bg-red-950/50 px-2 py-0.5 rounded border border-red-500/40">
                  <Zap size={12} />
                  تم كسر حجة الغياب وإثبات التناقض!
                </span>
              )}
            </div>

            {/* Dialogue Transcript Log */}
            <div className="flex-1 overflow-y-auto max-h-56 bg-zinc-950/90 rounded-xl p-3 border border-zinc-800 flex flex-col gap-2 text-xs md:text-sm">
              {dialogueLog.map((log, index) => (
                <div
                  key={index}
                  className={`p-2.5 rounded-lg ${
                    log.isAlert
                      ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40 font-bold'
                      : log.speaker === 'النقيب يوسف'
                      ? 'bg-blue-950/50 text-blue-200 mr-4 border-r-2 border-blue-500'
                      : 'bg-zinc-900 text-zinc-200 ml-4 border-l-2 border-amber-500'
                  }`}>
                  <span className="font-bold text-xs block mb-0.5 opacity-80">{log.speaker}:</span>
                  <p>{log.text}</p>
                </div>
              ))}
            </div>

            {/* Interrogation Options */}
            <div className="flex flex-col gap-2">
              <span className="text-xs font-bold text-amber-400">اختر سؤالاً أو قدم دليلاً للمواجهة:</span>
              <div className="grid grid-cols-1 gap-1.5 max-h-36 overflow-y-auto">
                {currentDialogueNodeId &&
                  selectedSuspectForInterrogation.dialogueNodes[currentDialogueNodeId]?.options.map((opt) => {
                    const isAvailable = !opt.requiredEvidenceId || caseProgress.discoveredEvidenceIds.includes(opt.requiredEvidenceId);
                    return (
                      <button
                        key={opt.id}
                        disabled={!isAvailable}
                        onClick={() => handleSelectDialogueOption(opt)}
                        className={`p-2.5 text-right rounded-xl text-xs md:text-sm font-medium border transition-all flex items-center justify-between ${
                          !isAvailable
                            ? 'bg-zinc-900/40 text-zinc-600 border-zinc-800 cursor-not-allowed'
                            : 'bg-zinc-800/90 hover:bg-amber-500/20 text-zinc-200 border-zinc-700 hover:border-amber-400'
                        }`}>
                        <span>{opt.textAr}</span>
                        {!isAvailable && (
                          <span className="text-[10px] bg-zinc-800 text-zinc-500 px-2 py-0.5 rounded">
                            يتطلب دليل إضافي
                          </span>
                        )}
                      </button>
                    );
                  })}
              </div>
            </div>

            <button
              onClick={() => setSelectedSuspectForInterrogation(null)}
              className="w-full py-2 bg-zinc-800 hover:bg-zinc-700 text-zinc-200 font-bold rounded-xl text-xs">
              إنهاء جلسة الاستجواب مؤقتاً
            </button>
          </div>
        </div>
      )}

      {/* NOTEBOOK TAB (دفتر التحقيق) */}
      {activeTab === 'notebook' && (
        <div className="fixed inset-0 z-40 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="w-full max-w-2xl bg-zinc-900 border-2 border-amber-500/40 rounded-2xl p-6 shadow-2xl flex flex-col gap-4 max-h-[85vh] overflow-hidden text-right">
            <div className="flex items-center justify-between border-b border-zinc-800 pb-3">
              <div className="flex items-center gap-2">
                <BookOpen className="text-amber-400" size={24} />
                <h2 className="text-lg font-bold text-amber-400">دفتر التحقيق الميداني</h2>
              </div>
              <button onClick={() => setActiveTab('none')} className="p-1.5 text-zinc-400 hover:text-white bg-zinc-800 rounded-lg">
                <X size={18} />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto grid grid-cols-1 md:grid-cols-2 gap-3 p-1">
              {CASE_ONE.evidenceList.map((ev) => {
                const isFound = caseProgress.discoveredEvidenceIds.includes(ev.id);
                return (
                  <div
                    key={ev.id}
                    onClick={() => isFound && handleOpenEvidence(ev)}
                    className={`p-3 rounded-xl border transition-all ${
                      isFound
                        ? 'bg-zinc-950/80 border-amber-500/40 cursor-pointer hover:border-amber-400'
                        : 'bg-zinc-950/30 border-zinc-800 text-zinc-600'
                    }`}>
                    <div className="flex items-center justify-between mb-1.5">
                      <span className="font-bold text-sm text-amber-300">
                        {isFound ? ev.titleAr : 'دليل لم يُكتشف بعد'}
                      </span>
                      <span className="text-[10px] px-2 py-0.5 rounded bg-zinc-800 text-zinc-400">
                        {ev.locationAr}
                      </span>
                    </div>
                    <p className="text-xs text-zinc-400 line-clamp-2">
                      {isFound ? ev.description : 'قم بتمشيط مسرح الجريمة وشوارع القاهرة لجمع هذا الأثر.'}
                    </p>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* TRAVEL MAP TAB */}
      {activeTab === 'map' && (
        <div className="fixed inset-0 z-40 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="w-full max-w-lg bg-zinc-900 border-2 border-amber-500/40 rounded-2xl p-6 shadow-2xl flex flex-col gap-4 text-right">
            <div className="flex items-center justify-between border-b border-zinc-800 pb-3">
              <div className="flex items-center gap-2">
                <Compass className="text-amber-400" size={24} />
                <h2 className="text-lg font-bold text-amber-400">خريطة مواقع التحقيق بالقاهرة</h2>
              </div>
              <button onClick={() => setActiveTab('none')} className="p-1.5 text-zinc-400 hover:text-white bg-zinc-800 rounded-lg">
                <X size={18} />
              </button>
            </div>

            <div className="flex flex-col gap-3">
              {CASE_ONE.locations.map((loc) => {
                const isCurrent = caseProgress.currentLocation === loc.id;
                return (
                  <button
                    key={loc.id}
                    disabled={isCurrent}
                    onClick={() => handleLocationChange(loc.id)}
                    className={`p-4 rounded-xl border text-right transition-all flex items-center justify-between ${
                      isCurrent
                        ? 'bg-amber-500/20 border-amber-400 text-amber-300'
                        : 'bg-zinc-950/60 hover:bg-zinc-800 border-zinc-800 text-zinc-200'
                    }`}>
                    <div>
                      <h3 className="font-bold text-sm mb-1">{loc.nameAr}</h3>
                      <p className="text-xs text-zinc-400">{loc.descriptionAr}</p>
                    </div>
                    {isCurrent ? (
                      <span className="text-xs font-bold text-amber-400 bg-amber-950/60 px-2 py-1 rounded">
                        الموقع الحالي
                      </span>
                    ) : (
                      <span className="text-xs text-zinc-400 bg-zinc-800 px-2 py-1 rounded">
                        انتقال ↵
                      </span>
                    )}
                  </button>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* FINAL ACCUSATION TAB */}
      {activeTab === 'accuse' && (
        <div className="fixed inset-0 z-40 bg-black/85 backdrop-blur-md flex items-center justify-center p-4">
          <div className="w-full max-w-xl bg-zinc-900 border-2 border-red-500/50 rounded-2xl p-6 shadow-2xl flex flex-col gap-4 text-right max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-zinc-800 pb-3">
              <div className="flex items-center gap-2">
                <Shield className="text-red-400" size={24} />
                <h2 className="text-lg font-bold text-red-400">جلسة توجيه الاتهام النهائي للنيابة</h2>
              </div>
              <button onClick={() => setActiveTab('none')} className="p-1.5 text-zinc-400 hover:text-white bg-zinc-800 rounded-lg">
                <X size={18} />
              </button>
            </div>

            <p className="text-xs text-zinc-300 leading-relaxed">
              بصفتك ضابط المباحث المسؤول، حدد المشتبه به الرئيسي، الدافع الحقيقي للجريمة، وأرفق الأدلة القاطعة لإحالته للجنايات.
            </p>

            {/* Suspect Selection */}
            <div>
              <label className="text-xs font-bold text-amber-400 block mb-1.5">1. اختيار المتهم الرئيسي:</label>
              <div className="grid grid-cols-2 gap-2">
                {CASE_ONE.suspects.map((s) => (
                  <button
                    key={s.id}
                    onClick={() => setAccusedSuspectId(s.id)}
                    className={`p-2.5 rounded-xl border text-right text-xs font-bold transition-all ${
                      accusedSuspectId === s.id
                        ? 'bg-red-950/80 border-red-400 text-red-200'
                        : 'bg-zinc-950 border-zinc-800 text-zinc-400 hover:bg-zinc-800'
                    }`}>
                    {s.nameAr} ({s.roleAr})
                  </button>
                ))}
              </div>
            </div>

            {/* Motive Selection */}
            <div>
              <label className="text-xs font-bold text-amber-400 block mb-1.5">2. الدافع الجنائي الأساسي:</label>
              <select
                value={accusedMotive}
                onChange={(e) => setAccusedMotive(e.target.value)}
                className="w-full bg-zinc-950 border border-zinc-800 rounded-xl p-2.5 text-xs text-zinc-200 focus:outline-none focus:border-amber-400">
                <option value="embezzlement">اختلاس مالي وتزوير شيكات وتهديد بفسخ الشراكة</option>
                <option value="marital">خلافات أسرية وزوجية متكررة</option>
                <option value="inheritance">نزاع على ميراث شقة العائلة</option>
                <option value="random">مشاجرة عشوائية بدافع السرقة</option>
              </select>
            </div>

            {/* Evidence Attachment */}
            <div>
              <label className="text-xs font-bold text-amber-400 block mb-1.5">
                3. تقديم الأدلة الجنائية الدامغة (اختر حتى 3 أدلة):
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 max-h-40 overflow-y-auto">
                {CASE_ONE.evidenceList.map((ev) => {
                  const isFound = caseProgress.discoveredEvidenceIds.includes(ev.id);
                  const isSelected = selectedProofIds.includes(ev.id);
                  return (
                    <button
                      key={ev.id}
                      disabled={!isFound}
                      onClick={() => {
                        if (isSelected) {
                          setSelectedProofIds(selectedProofIds.filter((id) => id !== ev.id));
                        } else if (selectedProofIds.length < 3) {
                          setSelectedProofIds([...selectedProofIds, ev.id]);
                        }
                      }}
                      className={`p-2 rounded-lg border text-right text-xs transition-all flex items-center justify-between ${
                        !isFound
                          ? 'bg-zinc-950/30 border-zinc-800 text-zinc-700 cursor-not-allowed'
                          : isSelected
                          ? 'bg-red-950/60 border-red-400 text-red-200 font-bold'
                          : 'bg-zinc-950 border-zinc-800 text-zinc-300 hover:bg-zinc-800'
                      }`}>
                      <span className="line-clamp-1">{ev.titleAr}</span>
                      {isSelected && <CheckCircle size={14} className="text-red-400 shrink-0" />}
                    </button>
                  );
                })}
              </div>
            </div>

            <button
              onClick={handleExecuteAccusation}
              className="w-full py-3 bg-red-600 hover:bg-red-500 text-white font-black rounded-xl text-sm transition-all shadow-lg active:scale-95">
              إصدار أمر الضبط والإحالة للنيابة
            </button>
          </div>
        </div>
      )}

      {/* START SCREEN OVERLAY */}
      {gameState.status === 'START' && (
        <MenuOverlay tone="neutral">
          <div className="bg-zinc-900/95 border-2 border-amber-500/40 p-6 md:p-8 rounded-2xl shadow-2xl flex flex-col items-center w-full max-w-md text-center backdrop-blur-md">
            <div className="w-16 h-16 rounded-full bg-amber-500/20 border border-amber-400 flex items-center justify-center text-amber-400 mb-4 shadow-lg">
              <Shield size={32} />
            </div>
            <h1 className="text-3xl md:text-4xl font-black text-amber-400 mb-2 font-sans tracking-wide">
              ملف مفتوح: MAFTUH
            </h1>
            <p className="text-xs md:text-sm text-zinc-300 mb-6 leading-relaxed">
              تحقيق جنائي ثلاثي الأبعاد في شوارع وشقق القاهرة. تقمص دور النقيب يوسف المصري لكشف ملابسات جريمة شقة مدينة نصر.
            </p>
            <button
              ref={startBtnRef}
              onClick={initGame}
              autoFocus
              data-testid="start-button"
              className="w-full bg-amber-500 text-zinc-950 px-6 py-3.5 rounded-xl font-black text-base hover:bg-amber-400 transition-all shadow-xl flex items-center justify-center gap-2 active:scale-95 mb-3">
              <Play size={20} />
              بدء التحقيق الميداني
            </button>
            <button
              onClick={() => setIsHowToPlayOpen(true)}
              className="w-full bg-zinc-800 text-zinc-200 px-4 py-2.5 rounded-xl font-bold text-xs hover:bg-zinc-700 transition-all">
              طريقة اللعب وقواعد التحقيق
            </button>
          </div>
        </MenuOverlay>
      )}

      {/* GAME MENU / PAUSE OVERLAY */}
      {isGameMenuOpen && (
        <MenuOverlay tone="neutral" zIndex={60}>
          <div className="w-full max-w-sm bg-zinc-900/95 border-2 border-amber-500/40 rounded-2xl p-6 shadow-2xl flex flex-col items-center gap-3 backdrop-blur-md">
            <h2 className="text-xl font-black text-amber-400 mb-2">قائمة التحقيق</h2>

            <button
              onClick={() => setIsGameMenuOpen(false)}
              className="w-full py-2.5 bg-amber-500 text-zinc-950 font-bold rounded-xl hover:bg-amber-400 text-sm">
              استئناف التحقيق
            </button>

            <div className="w-full grid grid-cols-2 gap-2 my-1">
              <button
                onClick={() => {
                  const muted = gameAudio.toggleMute();
                  setIsSfxMuted(muted);
                }}
                className="py-2 px-3 bg-zinc-800 text-zinc-200 border border-zinc-700 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5">
                {isSfxMuted ? <VolumeX size={14} /> : <Volume2 size={14} />}
                <span>المؤثرات: {isSfxMuted ? 'كتم' : 'تشغيل'}</span>
              </button>

              <button
                onClick={() => setIsMusicMuted(!isMusicMuted)}
                className="py-2 px-3 bg-zinc-800 text-zinc-200 border border-zinc-700 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5">
                {isMusicMuted ? <VolumeX size={14} /> : <Volume2 size={14} />}
                <span>الموسيقى: {isMusicMuted ? 'كتم' : 'تشغيل'}</span>
              </button>
            </div>

            <button
              onClick={() => {
                setIsHowToPlayOpen(true);
              }}
              className="w-full py-2 bg-zinc-800 hover:bg-zinc-700 text-zinc-200 font-bold rounded-xl text-xs">
              تعليمات التحقيق
            </button>

            <button
              onClick={() => {
                localStorage.removeItem(STORAGE_KEY);
                window.location.reload();
              }}
              className="w-full py-2 bg-zinc-800 hover:bg-red-950 text-red-400 font-bold rounded-xl text-xs">
              إعادة القضية من البداية
            </button>

            <button
              onClick={() => {
                setIsGameMenuOpen(false);
                setGameState((prev) => ({...prev, status: 'START'}));
              }}
              className="w-full py-2 bg-zinc-800 hover:bg-zinc-700 text-zinc-400 font-bold rounded-xl text-xs">
              الخروج للشاشة الرئيسية
            </button>
          </div>
        </MenuOverlay>
      )}

      {/* HOW TO PLAY OVERLAY */}
      {isHowToPlayOpen && (
        <div className="fixed inset-0 z-[70] bg-black/90 backdrop-blur-md flex items-center justify-center p-4">
          <div className="w-full max-w-md bg-zinc-900 border-2 border-amber-500/50 rounded-2xl p-6 shadow-2xl flex flex-col gap-4 text-right">
            <div className="flex items-center justify-between border-b border-zinc-800 pb-3">
              <div className="flex items-center gap-2">
                <HelpCircle className="text-amber-400" size={22} />
                <h2 className="text-base font-bold text-amber-400">دليل المحقق الجنائي</h2>
              </div>
              <button onClick={() => setIsHowToPlayOpen(false)} className="p-1.5 text-zinc-400 hover:text-white bg-zinc-800 rounded-lg">
                <X size={18} />
              </button>
            </div>

            <div className="flex flex-col gap-2.5 text-xs text-zinc-300 leading-relaxed">
              <div className="bg-zinc-950 p-3 rounded-xl border border-zinc-800">
                <span className="font-bold text-amber-400 block mb-1">1. الاستكشاف وجمع الأدلة:</span>
                تحرك في الشقة والشوارع واقترب من العناصر المشبوهة واضغط زر التفاعل لفحصها بدقة.
              </div>
              <div className="bg-zinc-950 p-3 rounded-xl border border-zinc-800">
                <span className="font-bold text-amber-400 block mb-1">2. الاستجواب وكشف التناقضات:</span>
                تحدث مع المشتبه بهم وقدم الأدلة المكتشفة لكسر حجج غيابهم واعترافهم بالحقيقة.
              </div>
              <div className="bg-zinc-950 p-3 rounded-xl border border-zinc-800">
                <span className="font-bold text-amber-400 block mb-1">3. توجيه الاتهام النهائي:</span>
                عند اكتمال خيوط القضية، ادخل شاشة توجيه الاتهام وقدم دليلك الحاسم للحصول على تقييم امتياز!
              </div>
            </div>

            <button
              onClick={() => setIsHowToPlayOpen(false)}
              className="w-full py-2.5 bg-amber-500 text-zinc-950 font-bold rounded-xl hover:bg-amber-400 text-xs">
              إغلاق الدليل
            </button>
          </div>
        </div>
      )}

      {/* VICTORY OVERLAY */}
      {gameState.status === 'VICTORY' && (
        <Overlay
          title={verdictResult === 'perfect' ? CASE_ONE.endings.perfect.titleAr : CASE_ONE.endings.partial.titleAr}
          description={
            verdictResult === 'perfect'
              ? `${CASE_ONE.endings.perfect.descriptionAr}\n\n${CASE_ONE.endings.perfect.ratingAr} — النتيجة: ${gameState.score}`
              : `${CASE_ONE.endings.partial.descriptionAr}\n\n${CASE_ONE.endings.partial.ratingAr} — النتيجة: ${gameState.score}`
          }
          actionLabel="لعب القضية مجدداً"
          onAction={() => {
            localStorage.removeItem(STORAGE_KEY);
            window.location.reload();
          }}
          icon={<RotateCw size={20} />}
          tone="victory"
        />
      )}

      {/* GAME OVER OVERLAY */}
      {gameState.status === 'GAME_OVER' && (
        <Overlay
          title={CASE_ONE.endings.wrong.titleAr}
          description={`${CASE_ONE.endings.wrong.descriptionAr}\n\n${CASE_ONE.endings.wrong.ratingAr} — النتيجة: ${gameState.score}`}
          actionLabel="إعادة التحقيق"
          onAction={() => {
            localStorage.removeItem(STORAGE_KEY);
            window.location.reload();
          }}
          icon={<RotateCw size={20} />}
          tone="danger"
        />
      )}
    </div>
  );
}
