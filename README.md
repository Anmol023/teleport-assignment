sample request:
``` json
{
  "truck": {
    "id": "truck-123",
    "max_weight_lbs": 44000,
    "max_volume_cuft": 3000
  },
  "orders": [
    {
      "id": "ord-001",
      "payout_cents": 250000,
      "weight_lbs": 18000,
      "volume_cuft": 1200,
      "origin": "Los Angeles, CA",
      "destination": "Dallas, TX",
      "pickup_date": "2025-12-05",
      "delivery_date": "2025-12-09",
      "is_hazmat": false
    },
    {
      "id": "ord-002",
      "payout_cents": 180000,
      "weight_lbs": 12000,
      "volume_cuft": 900,
      "origin": "Los Angeles, CA",
      "destination": "Dallas, TX",
      "pickup_date": "2025-12-04",
      "delivery_date": "2025-12-10",
      "is_hazmat": false
    },
    {
      "id": "ord-003",
      "payout_cents": 320000,
      "weight_lbs": 30000,
      "volume_cuft": 1800,
      "origin": "Los Angeles, CA",
      "destination": "Dallas, TX",
      "pickup_date": "2025-12-06",
      "delivery_date": "2025-12-08",
      "is_hazmat": true
    }
  ]
}
```

sample response:

``` json
{
  "truck_id": "truck-123",
  "selected_order_ids": [
    "ord-001",
    "ord-002"
  ],
  "total_payout_cents": 430000,
  "total_weight_lbs": 30000,
  "total_volume_cuft": 2100,
  "utilization_weight_percent": 68.18,
  "utilization_volume_percent": 70
}
```

## Remaining Tasks:-
1. Validate Request
2. Add Caching if same request no need to calculate again
3. Add configurable parameters for optimization (e.g. prioritize payout vs utilization)
4. Exception handling