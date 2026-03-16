import http from '@/utils/http';

export interface ResourceBalanceVO {
  uid: string;
  resourceType: string;
  totalAmount: number;
  totalBalance: number;
  memberTotal: number;
  memberBalance: number;
  buyTotal: number;
  buyBalance: number;
  activityTotal: number;
  activityBalance: number;
}

export interface ResourceFlowVO {
  id: number;
  flowNo: string;
  uid: string;
  resourceType: string;
  resourceTypeName: string;
  direction: number;
  directionName: string;
  amount: number;
  sourceType?: string;
  sourceTypeName?: string;
  reason?: string;
  remark?: string;
  createTime: string;
}

export interface ResourceQueryRequest {
  resourceType: string;
  direction?: number;
  startTime?: string;
  endTime?: string;
  pageNum?: number;
  pageSize?: number;
}

export async function getPointBalance(): Promise<ResourceBalanceVO> {
  return await http.get('/resource/balance', {
    params: {
      resourceType: 'POINT',
    },
  });
}

export async function queryPointFlow(
  params: ResourceQueryRequest
): Promise<ResourceFlowVO[]> {
  return await http.post('/resource/flow/query', params);
}
