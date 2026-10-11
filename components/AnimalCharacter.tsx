import {useFrame} from '@react-three/fiber';
import {forwardRef, useRef} from 'react';
import * as THREE from 'three';

const DEFAULT_POSITION: [number, number, number] = [0, 0, 0];
const DEFAULT_ROTATION: [number, number, number] = [0, 0, 0];

export interface ProceduralAnimalProps {
  type?: 'dog' | 'cat' | 'horse' | 't-rex' | 'chicken';
  color?: string;
  state?: 'idle' | 'walk' | 'run';
  size?: number;
  position?: [number, number, number];
  rotation?: [number, number, number];
  castShadow?: boolean;
}

export const AnimalCharacter = forwardRef<THREE.Group, ProceduralAnimalProps>(
  function AnimalCharacter(
    {
      type = 'cat',
      color = '#e2e8f0',
      state = 'idle',
      size = 0.8,
      position = DEFAULT_POSITION,
      rotation = DEFAULT_ROTATION,
      castShadow = true,
    }: ProceduralAnimalProps,
    ref,
  ) {
    const frontLeftLeg = useRef<THREE.Group>(null);
    const frontRightLeg = useRef<THREE.Group>(null);
    const backLeftLeg = useRef<THREE.Group>(null);
    const backRightLeg = useRef<THREE.Group>(null);

    const bodyArgs: [number, number, number] = [0.35, 0.35, 0.6];
    const legLength = 0.3;

    useFrame(({clock}) => {
      const t = clock.getElapsedTime();
      const speed = state === 'run' ? 14 : 7;
      const intensity = state === 'idle' ? 0.05 : 0.35;

      const backLeft = backLeftLeg.current;
      const backRight = backRightLeg.current;
      if (!backLeft || !backRight) return;

      const swing = Math.sin(t * speed) * intensity;
      backRight.rotation.x = swing;
      backLeft.rotation.x = -swing;
      if (frontLeftLeg.current) frontLeftLeg.current.rotation.x = -swing;
      if (frontRightLeg.current) frontRightLeg.current.rotation.x = swing;
    });

    return (
      <group ref={ref} position={position} rotation={rotation} scale={[size, size, size]}>
        {/* Body */}
        <mesh position={[0, legLength + bodyArgs[1] / 2, 0]} castShadow={castShadow}>
          <boxGeometry args={bodyArgs} />
          <meshStandardMaterial color={color} roughness={0.8} />
        </mesh>

        {/* Head */}
        <mesh position={[0, legLength + bodyArgs[1] * 0.8, -0.4]} castShadow={castShadow}>
          <boxGeometry args={[0.26, 0.24, 0.28]} />
          <meshStandardMaterial color={color} roughness={0.8} />
        </mesh>

        {/* Ears */}
        <mesh position={[-0.08, legLength + bodyArgs[1] * 1.2, -0.4]}>
          <coneGeometry args={[0.06, 0.12, 4]} />
          <meshStandardMaterial color={color} />
        </mesh>
        <mesh position={[0.08, legLength + bodyArgs[1] * 1.2, -0.4]}>
          <coneGeometry args={[0.06, 0.12, 4]} />
          <meshStandardMaterial color={color} />
        </mesh>

        {/* Legs */}
        <group ref={frontLeftLeg} position={[-0.12, legLength, -0.2]}>
          <Leg length={legLength} color={color} castShadow={castShadow} />
        </group>
        <group ref={frontRightLeg} position={[0.12, legLength, -0.2]}>
          <Leg length={legLength} color={color} castShadow={castShadow} />
        </group>
        <group ref={backLeftLeg} position={[-0.12, legLength, 0.2]}>
          <Leg length={legLength} color={color} castShadow={castShadow} />
        </group>
        <group ref={backRightLeg} position={[0.12, legLength, 0.2]}>
          <Leg length={legLength} color={color} castShadow={castShadow} />
        </group>
      </group>
    );
  },
);

function Leg({
  length,
  color,
  castShadow,
}: {
  length: number;
  color: string;
  castShadow: boolean;
}) {
  return (
    <group>
      <mesh position={[0, -length / 2, 0]} castShadow={castShadow}>
        <cylinderGeometry args={[0.04, 0.03, length]} />
        <meshStandardMaterial color={color} />
      </mesh>
    </group>
  );
}
