import { Button, DatePicker, Empty, Select, Spin, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { RangePickerProps } from 'antd/es/date-picker';
import dayjs, { Dayjs } from 'dayjs';
import { FC, memo, useCallback, useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import SiderContainer from '@/components/sider-container';
import {
  getPointBalance,
  queryPointFlow,
  ResourceBalanceVO,
  ResourceFlowVO,
} from '@/services/point';

const { RangePicker } = DatePicker;
const PAGE_SIZE = 20;

const EMPTY_BALANCE: ResourceBalanceVO = {
  uid: '',
  resourceType: 'POINT',
  totalAmount: 0,
  totalBalance: 0,
  memberTotal: 0,
  memberBalance: 0,
  buyTotal: 0,
  buyBalance: 0,
  activityTotal: 0,
  activityBalance: 0,
};

const PointPage: FC = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [balance, setBalance] = useState<ResourceBalanceVO>(EMPTY_BALANCE);
  const [flows, setFlows] = useState<ResourceFlowVO[]>([]);
  const [direction, setDirection] = useState<number>();
  const [timeRange, setTimeRange] = useState<[Dayjs, Dayjs] | null>(null);
  const [appliedDirection, setAppliedDirection] = useState<number>();
  const [appliedTimeRange, setAppliedTimeRange] = useState<
    [Dayjs, Dayjs] | null
  >(null);
  const [pageNum, setPageNum] = useState(1);
  const [hasNextPage, setHasNextPage] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  const loadData = useCallback(
    async (targetPageNum: number) => {
      setLoading(true);
      try {
        const [balanceData, flowData] = await Promise.all([
          getPointBalance(),
          queryPointFlow({
            resourceType: 'POINT',
            direction: appliedDirection,
            startTime: appliedTimeRange?.[0]?.format('YYYY-MM-DD HH:mm:ss'),
            endTime: appliedTimeRange?.[1]?.format('YYYY-MM-DD HH:mm:ss'),
            pageNum: targetPageNum,
            pageSize: PAGE_SIZE,
          }),
        ]);

        setBalance(balanceData);
        setFlows(flowData);
        setHasNextPage(flowData.length === PAGE_SIZE);
      } finally {
        setLoading(false);
      }
    },
    [appliedDirection, appliedTimeRange]
  );

  useEffect(() => {
    void loadData(pageNum);
  }, [loadData, pageNum, refreshKey]);

  const handleQuery = useCallback(() => {
    setAppliedDirection(direction);
    setAppliedTimeRange(timeRange);
    if (pageNum !== 1) {
      setPageNum(1);
    } else {
      setRefreshKey(current => current + 1);
    }
  }, [direction, pageNum, timeRange]);

  const handleReset = useCallback(() => {
    setDirection(undefined);
    setTimeRange(null);
    setAppliedDirection(undefined);
    setAppliedTimeRange(null);
    if (pageNum !== 1) {
      setPageNum(1);
    } else {
      setRefreshKey(current => current + 1);
    }
  }, [pageNum]);

  const handleRangeChange: RangePickerProps['onChange'] = value => {
    setTimeRange(value as [Dayjs, Dayjs] | null);
  };

  const columns = useMemo<ColumnsType<ResourceFlowVO>>(
    () => [
      {
        title: t('point.directionColumn'),
        dataIndex: 'directionName',
        key: 'directionName',
        width: 120,
        render: value => value || '--',
      },
      {
        title: t('point.amountColumn'),
        dataIndex: 'amount',
        key: 'amount',
        width: 120,
        render: value => value ?? 0,
      },
      {
        title: t('point.sourceColumn'),
        dataIndex: 'sourceTypeName',
        key: 'sourceTypeName',
        width: 140,
        render: value => value || '--',
      },
      {
        title: t('point.reasonColumn'),
        dataIndex: 'reason',
        key: 'reason',
        ellipsis: true,
        render: value => value || '--',
      },
      {
        title: t('point.remarkColumn'),
        dataIndex: 'remark',
        key: 'remark',
        ellipsis: true,
        render: value => value || '--',
      },
      {
        title: t('point.timeColumn'),
        dataIndex: 'createTime',
        key: 'createTime',
        width: 180,
        render: value =>
          value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '--',
      },
    ],
    [t]
  );

  const summaryCards = useMemo(
    () => [
      {
        key: 'total',
        title: t('point.totalBalance'),
        value: balance.totalBalance,
        subValue: balance.totalAmount,
      },
      {
        key: 'member',
        title: t('point.memberBalance'),
        value: balance.memberBalance,
        subValue: balance.memberTotal,
      },
      {
        key: 'buy',
        title: t('point.buyBalance'),
        value: balance.buyBalance,
        subValue: balance.buyTotal,
      },
      {
        key: 'activity',
        title: t('point.activityBalance'),
        value: balance.activityBalance,
        subValue: balance.activityTotal,
      },
    ],
    [balance, t]
  );

  return (
    <div className="w-full h-full overflow-hidden">
      <SiderContainer
        rightContent={
          <div className="flex flex-col gap-6">
            <div className="rounded-2xl bg-white p-6 shadow-sm">
              <div className="flex flex-col gap-2">
                <div className="text-[24px] font-semibold text-[#1f2329]">
                  {t('point.pageTitle')}
                </div>
                <div className="text-[14px] text-[#646a73]">
                  {t('point.pageDescription')}
                </div>
              </div>

              <div className="mt-6 grid grid-cols-1 gap-4 xl:grid-cols-4">
                {summaryCards.map(card => (
                  <div
                    key={card.key}
                    className="rounded-2xl border border-[#e5e6eb] bg-[#fafafa] px-5 py-4"
                  >
                    <div className="text-[14px] text-[#646a73]">
                      {card.title}
                    </div>
                    <div className="mt-3 text-[32px] font-semibold leading-none text-[#1d2129]">
                      {card.value}
                    </div>
                    <div className="mt-3 text-[12px] text-[#86909c]">
                      {t('point.totalGranted')}: {card.subValue}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="rounded-2xl bg-white p-6 shadow-sm">
              <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                <div>
                  <div className="text-[20px] font-semibold text-[#1f2329]">
                    {t('point.flowTitle')}
                  </div>
                </div>

                <div className="flex flex-col gap-3 lg:flex-row lg:items-center">
                  <Select
                    allowClear
                    className="w-full lg:w-[180px]"
                    placeholder={t('point.direction')}
                    value={direction}
                    onChange={value => {
                      setDirection(value);
                    }}
                    options={[
                      { label: t('point.directionAdd'), value: 1 },
                      { label: t('point.directionDeduct'), value: 2 },
                      { label: t('point.directionInvalidate'), value: 3 },
                      { label: t('point.directionRefund'), value: 4 },
                    ]}
                  />
                  <RangePicker
                    className="w-full lg:w-[360px]"
                    showTime
                    value={timeRange}
                    onChange={handleRangeChange}
                    placeholder={[t('point.startTime'), t('point.endTime')]}
                  />
                  <div className="flex gap-3">
                    <Button type="primary" onClick={handleQuery}>
                      {t('common.query')}
                    </Button>
                    <Button onClick={handleReset}>{t('common.reset')}</Button>
                  </div>
                </div>
              </div>

              <div className="mt-6">
                <Spin spinning={loading}>
                  <Table<ResourceFlowVO>
                    rowKey={record => record.id || record.flowNo}
                    columns={columns}
                    dataSource={flows}
                    pagination={false}
                    locale={{
                      emptyText: <Empty description={t('point.empty')} />,
                    }}
                    scroll={{ x: 960 }}
                  />
                </Spin>

                <div className="mt-4 flex items-center justify-end gap-3">
                  <span className="text-[14px] text-[#646a73]">
                    {t('point.pageIndicator', { page: pageNum })}
                  </span>
                  <Button
                    disabled={pageNum === 1 || loading}
                    onClick={() => {
                      setPageNum(current => Math.max(1, current - 1));
                    }}
                  >
                    {t('point.prevPage')}
                  </Button>
                  <Button
                    disabled={!hasNextPage || loading}
                    onClick={() => {
                      setPageNum(current => current + 1);
                    }}
                  >
                    {t('point.nextPage')}
                  </Button>
                </div>
              </div>
            </div>
          </div>
        }
      />
    </div>
  );
};

export default memo(PointPage);
