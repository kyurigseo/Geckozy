import { Outlet } from 'react-router-dom'

import Nav from '../nav/Nav'

import './Layout.scss'

const Layout = () => {
  return (
    <div className="layout">
      <Outlet />
      <Nav />
    </div>
  )
}

export default Layout