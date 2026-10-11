// tslint:disable
/* eslint-disable */
/** 3D Vector. */
export interface Vec3 {
  x: number;
  y: number;
  z: number;
}

/** 3D Position as a tuple. */
export type Position3D = [number, number, number];

/** Represents an object in the 3D scene. */
export interface Entity3D {
  id: string | number;
  type?: string;
  position: Vec3 | Position3D;
  rotation?: Vec3 | Position3D;
  scale?: Vec3 | Position3D;
  velocity?: Vec3;
  active?: boolean;
  [key: string]: any;
}

/** Represents a particle in a particle system. */
export interface Particle {
  id: string | number;
  position: Vec3 | Position3D;
  velocity?: Vec3 | Position3D;
  life: number;
  maxLife: number;
  color?: string;
  size?: number;
  [key: string]: any;
}

export type LocationId = 'apartment' | 'street' | 'station';

export interface EvidenceItem {
  id: string;
  title: string;
  titleAr: string;
  category: 'physical' | 'document' | 'forensic' | 'digital' | 'testimony';
  categoryAr: string;
  location: LocationId;
  locationAr: string;
  position: Position3D;
  description: string;
  forensicDetail: string;
  discovered: boolean;
  relatedSuspectId?: string;
  isKeyEvidence: boolean;
  iconName: string;
}

export interface DialogueOption {
  id: string;
  textAr: string;
  responseAr: string;
  requiredEvidenceId?: string;
  unlockEvidenceId?: string;
  breaksAlibi?: boolean;
  alibiBrokenTextAr?: string;
  leadsToNodeId?: string;
}

export interface DialogueNode {
  id: string;
  suspectLineAr: string;
  options: DialogueOption[];
}

export interface Suspect {
  id: string;
  nameAr: string;
  roleAr: string;
  age: number;
  location: LocationId;
  position: Position3D;
  avatarColor: string;
  clothesColor: string;
  pantsColor: string;
  skinColor: string;
  initialAlibiAr: string;
  alibiStatus: 'intact' | 'questionable' | 'broken';
  motiveAr: string;
  isGuilty: boolean;
  dialogueRootNodeId: string;
  dialogueNodes: Record<string, DialogueNode>;
  notesAr: string[];
}

export interface CaseData {
  id: string;
  titleAr: string;
  subtitleAr: string;
  dateAr: string;
  victimNameAr: string;
  victimAge: number;
  briefingAr: string;
  locations: {
    id: LocationId;
    nameAr: string;
    descriptionAr: string;
  }[];
  evidenceList: EvidenceItem[];
  suspects: Suspect[];
  correctCulpritId: string;
  requiredProofIds: string[];
  endings: {
    perfect: {
      titleAr: string;
      descriptionAr: string;
      ratingAr: string;
      score: number;
    };
    partial: {
      titleAr: string;
      descriptionAr: string;
      ratingAr: string;
      score: number;
    };
    wrong: {
      titleAr: string;
      descriptionAr: string;
      ratingAr: string;
      score: number;
    };
  };
}

export interface CaseProgress {
  discoveredEvidenceIds: string[];
  interrogatedSuspectIds: string[];
  brokenAlibiSuspectIds: string[];
  currentLocation: LocationId;
  notes: string[];
  flashlightOn: boolean;
  inspectedEvidenceId: string | null;
  activeSuspectId: string | null;
  activeDialogueNodeId: string | null;
  evidenceBoardLinks: {fromId: string; toId: string}[];
  verdictSubmitted: boolean;
  verdictResult?: 'perfect' | 'partial' | 'wrong';
  completedCases: string[];
}
