import { Outlet } from 'react-router-dom'

import Nav from '../nav/Nav'

// 하단 Nav가 보이는 페이지들을 감싸는 레이아웃. Nav가 필요 없는 페이지(온보딩 등)는 이 밖에 라우트를 둔다
const Layout = () => {
  return (
    <>
      <Outlet />
      <Nav />
    </>
  )
}

export default Layout
