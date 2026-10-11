import {useFrame} from '@react-three/fiber';
import {forwardRef, useRef} from 'react';
import * as THREE from 'three';

const DEFAULT_POSITION: [number, number, number] = [0, 0, 0];
const DEFAULT_ROTATION: [number, number, number] = [0, 0, 0];

export interface HumanoidCharacterProps {
  position?: [number, number, number];
  rotation?: [number, number, number];
  state?: 'idle' | 'walk' | 'run' | 'action';
  toolType?: 'none' | 'flashlight' | 'magnifier' | 'folder';
  skinColor?: string;
  clothesColor?: string;
  pantsColor?: string;
  hairColor?: string;
  hasHat?: boolean;
  hasTie?: boolean;
  isOfficer?: boolean;
  castShadow?: boolean;
}

export const HumanoidCharacter = forwardRef<
  THREE.Group,
  HumanoidCharacterProps
>(function HumanoidCharacter(
  {
    position = DEFAULT_POSITION,
    rotation = DEFAULT_ROTATION,
    state = 'idle',
    toolType = 'none',
    skinColor = '#fed7aa',
    clothesColor = '#334155',
    pantsColor = '#0f172a',
    hairColor = '#1c1917',
    hasHat = false,
    hasTie = true,
    isOfficer = false,
    castShadow = true,
  }: HumanoidCharacterProps,
  ref,
) {
  const leftLeg = useRef<THREE.Group>(null);
  const rightLeg = useRef<THREE.Group>(null);
  const leftArm = useRef<THREE.Group>(null);
  const rightArm = useRef<THREE.Group>(null);
  const headRef = useRef<THREE.Group>(null);

  useFrame(({clock}) => {
    const t = clock.getElapsedTime();
    const speed = state === 'run' ? 14 : 7;
    const isAction = state === 'action';
    const intensity = state === 'run' ? 0.75 : state === 'walk' ? 0.4 : 0.05;

    if (
      leftLeg.current &&
      rightLeg.current &&
      leftArm.current &&
      rightArm.current
    ) {
      if (state === 'idle') {
        // Subtle breathing animation
        leftArm.current.rotation.x = Math.sin(t * 2) * 0.03;
        rightArm.current.rotation.x = -Math.sin(t * 2) * 0.03;
        leftLeg.current.rotation.x = 0;
        rightLeg.current.rotation.x = 0;
        if (headRef.current) {
          headRef.current.rotation.y = Math.sin(t * 0.8) * 0.08;
        }
      } else {
        leftLeg.current.rotation.x = Math.sin(t * speed) * intensity;
        rightLeg.current.rotation.x = Math.sin(t * speed + Math.PI) * intensity;

        if (!isAction) {
          leftArm.current.rotation.x = Math.sin(t * speed + Math.PI) * intensity;
          if (toolType === 'flashlight') {
            rightArm.current.rotation.x = -0.7 + Math.sin(t * speed) * 0.1;
          } else {
            rightArm.current.rotation.x = Math.sin(t * speed) * intensity;
          }
        } else {
          rightArm.current.rotation.x = -Math.abs(Math.sin(t * 8)) * 0.8;
          leftArm.current.rotation.x = 0;
        }
      }
    }
  });

  return (
    <group ref={ref} position={position} rotation={rotation}>
      {/* Head & Face */}
      <group ref={headRef} position={[0, 1.8, 0]}>
        <mesh castShadow={castShadow}>
          <sphereGeometry args={[0.22, 16, 16]} />
          <meshStandardMaterial color={skinColor} roughness={0.7} />
        </mesh>

        {/* Hair */}
        <mesh position={[0, 0.08, -0.05]} castShadow={castShadow}>
          <boxGeometry args={[0.26, 0.16, 0.28]} />
          <meshStandardMaterial color={hairColor} roughness={0.9} />
        </mesh>

        {/* Eyes (Facing -Z) */}
        <mesh position={[-0.07, 0.02, -0.21]}>
          <sphereGeometry args={[0.03, 8, 8]} />
          <meshStandardMaterial color="#111827" />
        </mesh>
        <mesh position={[0.07, 0.02, -0.21]}>
          <sphereGeometry args={[0.03, 8, 8]} />
          <meshStandardMaterial color="#111827" />
        </mesh>

        {/* Moustache / Beard hint for detective look */}
        <mesh position={[0, -0.08, -0.21]}>
          <boxGeometry args={[0.1, 0.03, 0.03]} />
          <meshStandardMaterial color={hairColor} />
        </mesh>

        {/* Officer Hat if applicable */}
        {hasHat && (
          <group position={[0, 0.2, 0]}>
            <mesh castShadow={castShadow}>
              <cylinderGeometry args={[0.24, 0.26, 0.12, 16]} />
              <meshStandardMaterial color="#0f172a" />
            </mesh>
            <mesh position={[0, -0.04, -0.16]} rotation={[0.2, 0, 0]} castShadow={castShadow}>
              <boxGeometry args={[0.28, 0.02, 0.16]} />
              <meshStandardMaterial color="#020617" />
            </mesh>
            <mesh position={[0, 0.02, -0.24]}>
              <boxGeometry args={[0.06, 0.06, 0.02]} />
              <meshStandardMaterial color="#eab308" metalness={0.8} />
            </mesh>
          </group>
        )}
      </group>

      {/* Torso & Suit Jacket */}
      <group position={[0, 1.15, 0]}>
        <mesh castShadow={castShadow}>
          <boxGeometry args={[0.48, 0.75, 0.28]} />
          <meshStandardMaterial color={clothesColor} roughness={0.8} />
        </mesh>

        {/* Collar & Tie */}
        {hasTie && (
          <>
            <mesh position={[0, 0.25, -0.15]}>
              <boxGeometry args={[0.18, 0.12, 0.02]} />
              <meshStandardMaterial color="#f8fafc" />
            </mesh>
            <mesh position={[0, 0.05, -0.16]}>
              <boxGeometry args={[0.06, 0.35, 0.02]} />
              <meshStandardMaterial color="#991b1b" />
            </mesh>
          </>
        )}

        {/* Officer Police Badge / Star */}
        {isOfficer && (
          <mesh position={[-0.14, 0.15, -0.15]}>
            <boxGeometry args={[0.06, 0.07, 0.02]} />
            <meshStandardMaterial color="#eab308" metalness={0.9} roughness={0.2} />
          </mesh>
        )}
      </group>

      {/* Arms */}
      <group ref={leftArm} position={[-0.34, 1.4, 0]}>
        <Limb color={clothesColor} skinColor={skinColor} castShadow={castShadow} />
      </group>

      <group ref={rightArm} position={[0.34, 1.4, 0]}>
        <Limb
          color={clothesColor}
          skinColor={skinColor}
          hasTool={toolType !== 'none'}
          toolType={toolType}
          castShadow={castShadow}
        />
      </group>

      {/* Legs & Shoes */}
      <group ref={leftLeg} position={[-0.14, 0.7, 0]}>
        <Limb color={pantsColor} isLeg castShadow={castShadow} />
      </group>
      <group ref={rightLeg} position={[0.14, 0.7, 0]}>
        <Limb color={pantsColor} isLeg castShadow={castShadow} />
      </group>
    </group>
  );
});

function Limb({
  color,
  skinColor = '#fed7aa',
  hasTool,
  toolType,
  isLeg,
  castShadow,
}: {
  color: string;
  skinColor?: string;
  hasTool?: boolean;
  toolType?: string;
  isLeg?: boolean;
  castShadow: boolean;
}) {
  return (
    <group>
      <mesh position={[0, -0.32, 0]} castShadow={castShadow}>
        <cylinderGeometry args={[0.08, 0.07, 0.65, 12]} />
        <meshStandardMaterial color={color} roughness={0.8} />
      </mesh>

      {/* Hand or Shoe */}
      {!isLeg && (
        <mesh position={[0, -0.66, 0]} castShadow={castShadow}>
          <sphereGeometry args={[0.06, 12, 12]} />
          <meshStandardMaterial color={skinColor} />
        </mesh>
      )}

      {isLeg && (
        <mesh position={[0, -0.65, -0.06]} castShadow={castShadow}>
          <boxGeometry args={[0.15, 0.1, 0.26]} />
          <meshStandardMaterial color="#020617" roughness={0.5} />
        </mesh>
      )}

      {/* Detective Flashlight */}
      {hasTool && toolType === 'flashlight' && (
        <group position={[0, -0.7, -0.15]} rotation={[Math.PI / 4, 0, 0]}>
          <mesh castShadow={castShadow}>
            <cylinderGeometry args={[0.03, 0.03, 0.25, 12]} />
            <meshStandardMaterial color="#1f2937" metalness={0.8} />
          </mesh>
          <mesh position={[0, 0.14, 0]}>
            <cylinderGeometry args={[0.05, 0.03, 0.06, 12]} />
            <meshStandardMaterial color="#fbbf24" emissive="#fbbf24" emissiveIntensity={0.8} />
          </mesh>
        </group>
      )}

      {/* Evidence Folder */}
      {hasTool && toolType === 'folder' && (
        <mesh position={[0, -0.7, -0.1]} rotation={[0.4, 0.2, 0]} castShadow={castShadow}>
          <boxGeometry args={[0.22, 0.3, 0.04]} />
          <meshStandardMaterial color="#78350f" />
        </mesh>
      )}
    </group>
  );
}
