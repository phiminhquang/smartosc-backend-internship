import { useEffect, useState, type FormEvent } from 'react'
import { useAuth } from '../auth/useAuth'
import {
  createDevice,
  deleteDevice,
  getDevices,
} from '../services/deviceService'
import { getErrorMessage } from '../services/http'
import type {
  DeviceCategory,
  DeviceCreationRequest,
  DeviceResponse,
  DeviceState,
  SpringPage,
} from '../types/api'

const CATEGORY_LABELS: Record<DeviceCategory, string> = {
  LAPTOP: 'Laptop',
  MONITOR: 'Màn hình',
  PHONE: 'Điện thoại',
}

const STATE_BADGES: Record<DeviceState, { label: string; className: string }> = {
  AVAILABLE: { label: 'Sẵn sàng', className: 'badge-available' },
  ASSIGNED: { label: 'Đang cấp phát', className: 'badge-assigned' },
  UNDER_REPAIR: { label: 'Đang sửa chữa', className: 'badge-repair' },
}

export function DashboardPage() {
  const { logout } = useAuth()

  const [devicePage, setDevicePage] = useState<SpringPage<DeviceResponse> | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [keyword, setKeyword] = useState('')
  const [selectedCategory, setSelectedCategory] = useState<DeviceCategory | ''>('')
  const [selectedState, setSelectedState] = useState<DeviceState | ''>('')
  const [currentPage, setCurrentPage] = useState(0)

  const [isModalOpen, setIsModalOpen] = useState(false)
  const [newCategory, setNewCategory] = useState<DeviceCategory>('LAPTOP')
  const [newName, setNewName] = useState('')
  const [newModel, setNewModel] = useState('')
  const [newDescription, setNewDescription] = useState('')
  const [creating, setCreating] = useState(false)
  const [modalError, setModalError] = useState('')

  async function loadDevices(pageToLoad = currentPage) {
    setLoading(true)
    setError('')
    try {
      const data = await getDevices({
        keyword: keyword.trim() || undefined,
        category: selectedCategory || undefined,
        state: selectedState || undefined,
        page: pageToLoad,
        size: 10,
      })
      setDevicePage(data)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadDevices(currentPage)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, selectedCategory, selectedState])

  function handleSearchSubmit(event: FormEvent) {
    event.preventDefault()
    setCurrentPage(0)
    loadDevices(0)
  }

  async function handleCreateDevice(event: FormEvent) {
    event.preventDefault()
    setModalError('')
    setCreating(true)

    const payload: DeviceCreationRequest = {
      category: newCategory,
      name: newName.trim(),
      model: newModel.trim(),
      description: newDescription.trim() || undefined,
    }

    try {
      await createDevice(payload)
      setIsModalOpen(false)
      setNewName('')
      setNewModel('')
      setNewDescription('')
      setCurrentPage(0)
      loadDevices(0)
    } catch (err) {
      setModalError(getErrorMessage(err))
    } finally {
      setCreating(false)
    }
  }

  async function handleDelete(id: string, name: string) {
    if (!window.confirm(`Bạn có chắc chắn muốn xóa thiết bị "${name}" không?`)) {
      return
    }

    try {
      await deleteDevice(id)
      loadDevices(currentPage)
    } catch (err) {
      alert(getErrorMessage(err))
    }
  }

  return (
    <main className="dashboard-page">
      <header className="topbar">
        <div>
          <p className="eyebrow">DEVICE MANAGEMENT</p>
          <strong>Hệ thống Quản lý Thiết bị</strong>
        </div>
        <button className="secondary-button" type="button" onClick={logout}>
          Đăng xuất
        </button>
      </header>

      <section className="section-header">
        <div>
          <h2>Danh sách Thiết bị</h2>
          <p className="section-subtitle">
            Dữ liệu kết nối trực tiếp từ Spring Boot qua <code>GET /api/devices</code>
          </p>
        </div>
        <button
          className="primary-button"
          type="button"
          onClick={() => setIsModalOpen(true)}
        >
          + Thêm thiết bị mới
        </button>
      </section>

      <form className="filter-bar" onSubmit={handleSearchSubmit}>
        <input
          type="text"
          placeholder="Tìm theo tên thiết bị hoặc serial..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />

        <select
          value={selectedCategory}
          onChange={(e) => {
            setSelectedCategory(e.target.value as DeviceCategory | '')
            setCurrentPage(0)
          }}
        >
          <option value="">Tất cả danh mục</option>
          <option value="LAPTOP">Laptop</option>
          <option value="MONITOR">Màn hình</option>
          <option value="PHONE">Điện thoại</option>
        </select>

        <select
          value={selectedState}
          onChange={(e) => {
            setSelectedState(e.target.value as DeviceState | '')
            setCurrentPage(0)
          }}
        >
          <option value="">Tất cả trạng thái</option>
          <option value="AVAILABLE">Sẵn sàng</option>
          <option value="ASSIGNED">Đang cấp phát</option>
          <option value="UNDER_REPAIR">Đang sửa chữa</option>
        </select>

        <button className="secondary-button" type="submit">
          Tìm kiếm
        </button>
      </form>

      {error && <div className="error-message">{error}</div>}

      <div className="table-container">
        {loading ? (
          <div className="loading-state">Đang tải dữ liệu từ máy chủ...</div>
        ) : !devicePage || devicePage.content.length === 0 ? (
          <div className="empty-state">Không có thiết bị nào phù hợp.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Serial Number</th>
                <th>Tên thiết bị</th>
                <th>Model</th>
                <th>Danh mục</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {devicePage.content.map((device) => {
                const badge = STATE_BADGES[device.state]
                return (
                  <tr key={device.id}>
                    <td>
                      <code>{device.serialNumber}</code>
                    </td>
                    <td>
                      <strong>{device.name}</strong>
                    </td>
                    <td>{device.model}</td>
                    <td>{CATEGORY_LABELS[device.category]}</td>
                    <td>
                      <span className={`status-pill ${badge?.className}`}>
                        {badge?.label ?? device.state}
                      </span>
                    </td>
                    <td>
                      <button
                        className="delete-button"
                        type="button"
                        onClick={() => handleDelete(device.id, device.name)}
                      >
                        Xóa
                      </button>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>

      {devicePage && devicePage.totalPages > 1 && (
        <div className="pagination">
          <button
            type="button"
            className="secondary-button"
            disabled={devicePage.first}
            onClick={() => setCurrentPage((prev) => Math.max(0, prev - 1))}
          >
            Trang trước
          </button>
          <span>
            Trang {devicePage.number + 1} / {devicePage.totalPages} (Tổng{' '}
            {devicePage.totalElements} thiết bị)
          </span>
          <button
            type="button"
            className="secondary-button"
            disabled={devicePage.last}
            onClick={() => setCurrentPage((prev) => prev + 1)}
          >
            Trang sau
          </button>
        </div>
      )}

      {isModalOpen && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <div className="modal-header">
              <h3>Thêm thiết bị mới</h3>
              <button
                type="button"
                className="close-button"
                onClick={() => setIsModalOpen(false)}
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleCreateDevice}>
              {modalError && <p className="error-message">{modalError}</p>}

              <label>
                Danh mục:
                <select
                  value={newCategory}
                  onChange={(e) => setNewCategory(e.target.value as DeviceCategory)}
                  required
                >
                  <option value="LAPTOP">Laptop</option>
                  <option value="MONITOR">Màn hình</option>
                  <option value="PHONE">Điện thoại</option>
                </select>
              </label>

              <label>
                Tên thiết bị:
                <input
                  type="text"
                  placeholder="Ví dụ: Macbook Pro 16 inch"
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  required
                />
              </label>

              <label>
                Model:
                <input
                  type="text"
                  placeholder="Ví dụ: M3 Max 36GB"
                  value={newModel}
                  onChange={(e) => setNewModel(e.target.value)}
                  required
                />
              </label>

              <label>
                Mô tả (tùy chọn):
                <input
                  type="text"
                  placeholder="Ghi chú thêm về thiết bị..."
                  value={newDescription}
                  onChange={(e) => setNewDescription(e.target.value)}
                />
              </label>

              <div className="modal-actions">
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => setIsModalOpen(false)}
                  disabled={creating}
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="primary-button"
                  disabled={creating}
                >
                  {creating ? 'Đang tạo...' : 'Lưu thiết bị'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </main>
  )
}
