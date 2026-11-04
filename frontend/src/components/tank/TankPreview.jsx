import { TANK_PARTS } from './tankParts'

import wallCream from '../../assets/onboarding/tank-wall-cream.png'

import './TankPreview.scss'

// 크림 벽지(사육장 본체) 위치 [left, top, width] (무대 기준 px)
const CREAM_WALL_RECT = [55, 159, 280.5]

// 사육장 본체: 벽지·빈 사육장 이미지가 각각 따로 그려져 안쪽 모양이 조금씩 달라서,
// 기준인 크림 벽지 이미지 하나를 쓰고 벽면 색만 바꾼다 (TankPreview.scss의 .tank-preview-wall-tint-*)
// 빈 사육장은 회색, 초록·노랑은 각 벽지 이미지의 벽 색에 맞춘 필터
// TODO: 같은 원본에서 벽 색만 바꾼 빈 사육장·벽지 이미지를 받으면 이미지로 교체

// 바닥재: 사육장 바닥 마름모(무대 기준 꼭짓점 왼(80,350.7) 뒤(193.3,293.8) 오른(308.1,352.2) 앞(194.8,415))에
// 각 바닥재 이미지의 윗면을 늘려 맞추고 바닥 모양대로 잘라낸다 (이미지 위치는 tankParts의 fill)
const FLOOR_AREA = { left: 80, top: 293.8, width: 228.1, height: 121.3 }

const rectStyle = ([left, top, width, height]) => ({ left, top, width, height })

// 사육장 미리보기 (마이·온보딩 사육장 커스텀, 홈 일러스트 공통)
// 벽지 또는 빈 사육장 위에 바닥재·덩굴·구조물을 겹친다. 부모 요소를 꽉 채우고(inset 0), 좌표는 커스텀 화면 무대(393x431) 기준
// tank: { wall, floor, vine, structure } (form.tank·getMyTank()와 같은 모양, 빈 값이면 안 그림)
// className: 쓰는 화면에서 크기·위치를 조절할 때
const TankPreview = ({ tank, className }) => {
  const selectedOption = (part) => part.options.find((option) => option.value === tank?.[part.type])
  const [wallPart, floorPart, ...decorParts] = TANK_PARTS
  const wallTint = selectedOption(wallPart)?.value ?? 'empty'
  const floor = selectedOption(floorPart)

  return (
    <div className={className ? `tank-preview ${className}` : 'tank-preview'} aria-label="내 사육장 미리보기" role="img">
      <img src={wallCream} alt="" style={rectStyle(CREAM_WALL_RECT)} />
      {wallTint !== 'cream' && (
        <img className={`tank-preview-wall-tint tank-preview-wall-tint-${wallTint}`} src={wallCream} alt="" style={rectStyle(CREAM_WALL_RECT)} />
      )}
      {floor && (
        <div className="tank-preview-floor" style={FLOOR_AREA}>
          <div className="tank-preview-floor-fill">
            <img src={floor.image} alt="" style={rectStyle(floor.fill)} />
          </div>
        </div>
      )}
      {decorParts.map((part) => {
        const option = selectedOption(part)
        return (
          option && (
            <img key={part.type} className={`tank-preview-${part.type}`} src={option.image} alt="" style={rectStyle(option.rect)} />
          )
        )
      })}
    </div>
  )
}

export default TankPreview
