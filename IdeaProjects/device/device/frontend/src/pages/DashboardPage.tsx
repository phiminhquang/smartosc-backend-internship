import { useAuth } from '../auth/useAuth'

const nextFeatures = [
  'Hiển thị thống kê thiết bị',
  'Danh sách và tìm kiếm thiết bị',
  'Cấp phát và trả thiết bị',
]

export function DashboardPage() {
  const { logout } = useAuth()

  return (
    <main className="dashboard-page">
      <header className="topbar">
        <div>
          <p className="eyebrow">DEVICE MANAGEMENT</p>
          <strong>Trang quản trị</strong>
        </div>
        <button className="secondary-button" type="button" onClick={logout}>
          Đăng xuất
        </button>
      </header>

      <section className="welcome-card">
        <div>
          <p className="eyebrow">BƯỚC ĐẦU TIÊN ĐÃ XONG</p>
          <h1>Frontend đã đăng nhập được vào Spring Boot.</h1>
          <p>
            JWT đang được giữ trong session của tab trình duyệt và tự động gắn
            vào các request API tiếp theo.
          </p>
        </div>
        <div className="status-badge">API connected</div>
      </section>

      <section className="next-grid">
        {nextFeatures.map((feature, index) => (
          <article key={feature} className="next-card">
            <span>0{index + 1}</span>
            <h2>{feature}</h2>
          </article>
        ))}
      </section>
    </main>
  )
}
