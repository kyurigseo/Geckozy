import leafIcon from '../../../../assets/onboarding/icon-leaf.png'

import './LeafTitle.scss'

// 나뭇잎 아이콘 + 항목 제목 (피그마 '이름', '도마뱀 종' 등)
const LeafTitle = ({ children, as: Tag = 'h3' }) => {
  return (
    <Tag className="leaf-title">
      <img src={leafIcon} alt="" />
      {children}
    </Tag>
  )
}

export default LeafTitle
