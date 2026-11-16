import wallCream from '../../assets/onboarding/tank-wall-cream.png'
import wallGreen from '../../assets/onboarding/tank-wall-green.png'
import wallYellow from '../../assets/onboarding/tank-wall-yellow.png'
import floorBark from '../../assets/onboarding/tank-floor-bark.png'
import floorSand from '../../assets/onboarding/tank-floor-sand.png'
import floorGrass from '../../assets/onboarding/tank-floor-grass.png'
import vineHeart from '../../assets/onboarding/tank-vine-heart.png'
import vineCurly from '../../assets/onboarding/tank-vine-curly.png'
import vineLeaf from '../../assets/onboarding/tank-vine-leaf.png'
import structureLog from '../../assets/onboarding/tank-structure-log.png'
import structureRock from '../../assets/onboarding/tank-structure-rock.png'
import structureStump from '../../assets/onboarding/tank-structure-stump.png'

// 사육장 꾸미기 항목 (TankPreview 미리보기·TankCustomScreen 선택 카드 공통)
// 피그마 순서. type: 섹션 키 (form.tank·getMyTank()의 키와 같음)
// 미리보기에서 각 이미지의 위치 [left, top, width, height?] (무대 기준 px)
// 이미지마다 캔버스 여백이 달라서, 그림 영역이 피그마 예시(크림 벽지·하트 덩굴·통나무)와 겹치도록 계산한 값
// floor의 fill: 바닥 마름모로 잘라낸 영역 안에서 이미지 위치 [left, top, width, height] (TankPreview의 FLOOR_AREA)
export const TANK_PARTS = [
  {
    type: 'wall',
    title: '벽지',
    options: [
      { value: 'cream', label: '크림 벽지', image: wallCream },
      { value: 'green', label: '초록 벽지', image: wallGreen },
      { value: 'yellow', label: '노랑 벽지', image: wallYellow },
    ],
  },
  {
    type: 'floor',
    title: '바닥재',
    options: [
      { value: 'bark', label: '나무껍질 바닥재', image: floorBark, fill: [-10.5, -68.9, 244.1, 204.9] },
      { value: 'sand', label: '모래 바닥재', image: floorSand, fill: [-7, -66.8, 242.5, 198.3] },
      { value: 'grass', label: '잔디 바닥재', image: floorGrass, fill: [-4.8, -50, 238.7, 184.8] },
    ],
  },
  {
    type: 'vine',
    title: '덩굴',
    options: [
      { value: 'heart', label: '하트잎 덩굴', image: vineHeart, rect: [73.6, 191.9, 133.1] },
      { value: 'curly', label: '꼬불 덩굴', image: vineCurly, rect: [78.4, 195.3, 124.9] },
      { value: 'leaf', label: '잎사귀 덩굴', image: vineLeaf, rect: [69.2, 196.7, 131.1] },
    ],
  },
  {
    type: 'structure',
    title: '구조물',
    options: [
      { value: 'log', label: '통나무 은신처', image: structureLog, rect: [96, 253, 156] },
      { value: 'rock', label: '바위 은신처', image: structureRock, rect: [91.9, 258.8, 159.3] },
      { value: 'stump', label: '그루터기', image: structureStump, rect: [87.6, 259.9, 163.3] },
    ],
  },
]
