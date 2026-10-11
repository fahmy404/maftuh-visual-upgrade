// tslint:disable
/* eslint-disable */

function getRequestedPlayersCount(): number {
  const requested = Number(
    new URLSearchParams(window.location.search).get('players') ?? '1',
  );
  return Number.isInteger(requested) && requested > 0 ? requested : 1;
}

export class FakePlaygroundSDK {
  private isInitialized = false;
  private onGameEvent: ((event: any) => void) | null = null;
  private onPlayerPresence: ((presence: any) => void) | null = null;
  private onLobbyStatus: ((status: any) => void) | null = null;
  private onError: ((error: any) => void) | null = null;

  private lobbyId = 'fake-lobby-123';
  private gameId = 'fake-game-456';

  private myPlayerId = 'player-1';
  private isHost = true;
  private players: any[] = [];
  private channel!: BroadcastChannel;
  private playersCount = 1;

  constructor() {
    const injectedPlayerId = (window as any).__PLAYER_ID__;
    if (injectedPlayerId) {
      this.myPlayerId = injectedPlayerId;
      this.isHost = injectedPlayerId === 'player-1';
      this.channel = new BroadcastChannel(`playground-lobby-${this.lobbyId}`);
      this.channel.onmessage = (event) => this._handleChannelMessage(event.data);
    }
  }

  async init(
    onGameEvent: (event: any) => void,
    onPlayerPresence: (presence: any) => void,
    onLobbyStatus?: (status: any) => void,
    onError?: (error: any) => void,
  ): Promise<void> {
    if (this.isInitialized) return;

    this.onGameEvent = onGameEvent;
    this.onPlayerPresence = onPlayerPresence;
    if (onLobbyStatus) this.onLobbyStatus = onLobbyStatus;
    if (onError) this.onError = onError;
    this.isInitialized = true;

    this.playersCount = (window as any).__PLAYERS_COUNT__ || 1;
    window.parent.postMessage({type: 'PREVIEW_READY'}, '*');

    this.players = [
      {
        playerId: 'player-1',
        displayName: 'Youssef (Detective)',
        isCurrentPlayer: true,
        isHost: true,
        score: 0,
      },
    ];
  }

  startGame(lobbyId?: string): void {
    this._triggerLobbyStatus('GAME_IN_PROGRESS');
  }

  scoreUpdated(scoreData: any): void {
    const me = this.players.find((p) => p.playerId === this.myPlayerId);
    if (me) {
      me.score = scoreData.value || 0;
    }
  }

  levelComplete(level: number | string): void {
    console.log(`Level Complete: ${level}`);
  }

  gameOver(lobbyId?: string): void {
    this._triggerLobbyStatus('GAME_ENDED');
  }

  pauseStateChanged(isPaused: boolean): void {}

  private _triggerLobbyStatus(status: string) {
    if (this.onLobbyStatus) {
      this.onLobbyStatus({
        status,
        lobbyId: this.lobbyId,
      });
    }
  }

  private _handleChannelMessage(data: any) {}
}
