import type {
  ApiResponse,
  DeviceCreationRequest,
  DeviceResponse,
  DeviceSearchParams,
  DeviceUpdateRequest,
  SpringPage,
} from '../types/api'
import { http } from './http'

export async function getDevices(
  params?: DeviceSearchParams,
): Promise<SpringPage<DeviceResponse>> {
  const response = await http.get<ApiResponse<SpringPage<DeviceResponse>>>(
    '/devices',
    { params },
  )

  if (!response.data.result) {
    throw new Error('Không thể tải danh sách thiết bị.')
  }

  return response.data.result
}

export async function createDevice(
  request: DeviceCreationRequest,
): Promise<DeviceResponse> {
  const response = await http.post<ApiResponse<DeviceResponse>>(
    '/devices',
    request,
  )

  if (!response.data.result) {
    throw new Error('Không thể tạo thiết bị mới.')
  }

  return response.data.result
}

export async function updateDevice(
  id: string,
  request: DeviceUpdateRequest,
): Promise<DeviceResponse> {
  const response = await http.put<ApiResponse<DeviceResponse>>(
    `/devices/${id}`,
    request,
  )

  if (!response.data.result) {
    throw new Error('Không thể cập nhật thiết bị.')
  }

  return response.data.result
}

export async function deleteDevice(id: string): Promise<void> {
  await http.delete<ApiResponse<void>>(`/devices/${id}`)
}
