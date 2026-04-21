# SmartLoad Optimization API
## How to run
```bash
    git clone <your-repo>
    cd <folder>
    docker compose up --build (Note: `MAX_PAYOUT` is the default strategy; see [Strategies](#strategies) for other options).
# → Service will be available at http://localhost:8080
```

# Health Check
```bash
    curl http://localhost:8080/actuator/health
# → Should return 200 OK with message "Service is healthy"
```

# Strategies
### For Configuring weight from docker compose
### MAX_PAYOUT (default behaviour) -> orders which increases payout the most are considered
### MAX_UTILIZATION -> orders which increases utilization the most are considered
```bash
    # For payout optimization (default)
    LOAD_OPTIMIZER_STRATEGY=MAX_PAYOUT \
    docker compose up --build

    # For utilization optimization
    LOAD_OPTIMIZER_STRATEGY=MAX_UTILIZATION \
    docker compose up --build
```
## BALANCED -> orders whihc scores best on a combined payout/utilization metric are considered
```bash
  LOAD_OPTIMIZER_STRATEGY=BALANCED \
  LOAD_OPTIMIZER_WEIGHTS_PAYOUT=60 \
  LOAD_OPTIMIZER_WEIGHTS_WEIGHT_UTILIZATION=20 \
  LOAD_OPTIMIZER_WEIGHTS_VOLUME_UTILIZATION=20 \
  docker compose up --build
``` -> LOAD_OPTIMIZER_WEIGHTS_PAYOUT + LOAD_OPTIMIZER_WEIGHTS_WEIGHT_UTILIZATION + LOAD_OPTIMIZER_WEIGHTS_VOLUME_UTILIZATION should be 100

# Constraints for the load optimization:
## 1. Per-order date validity
Each order must satisfy `pickupDate ≤ deliveryDate`. Orders that violate
this are discarded before optimisation begins

## 2. Weight/Volume capacity
The combined weight of all selected orders must not exceed `truck.maxWeightLbs` || `truck.maxVolumeCuft`.
Any subset that breaches this are skipped.

## 4. Hazmat isolation
A hazmat order must be loaded alone — it cannot share the truck with any non-hazmat order. 
Multiple hazmat orders on the same lane are also not combined (a hazmat load is always exactly one order)

## 5. Same-lane grouping
Orders are grouped by `origin → destination` before optimisation. 
Orders on different lanes (i.e. A→B vs B→C) do not compete for space on the same truck. 
Only the best single lane is selected per dispatch

## Date overlap
As mentioned in task(same lane may have overlapping or identical pickup/delivery windows) can combine freely as long as they satisfy the above constraints.


# Sample request in the repo :
``` bash
curl -X POST "http://localhost:8080/api/v1/load-optimizer/optimize" \
  -H "Content-Type: application/json" \
  -d @request.json
```
## Sample response:

``` json
{
   "truck_id":"truck-max-test",
   "selected_order_ids":[
      "ord-001",
      "ord-002",
      "ord-003",
      "ord-004",
      "ord-005",
      "ord-006",
      "ord-007",
      "ord-008",
      "ord-009",
      "ord-010",
      "ord-011",
      "ord-012",
      "ord-013",
      "ord-014",
      "ord-015",
      "ord-016",
      "ord-017",
      "ord-018",
      "ord-019",
      "ord-020",
      "ord-021",
      "ord-022"
   ],
   "total_payout_cents":22000,
   "total_weight_lbs":2200,
   "total_volume_cuft":220,
   "utilization_weight_percent":2.2,
   "utilization_volume_percent":0.22
}
```