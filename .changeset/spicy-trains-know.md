---
"components-android": major
---

Compose depends on the timing of reads of `State` objects to determine whether it is a dependency for certain
use cases, such as when using `derivedStateOf` or `snapshotFlow`. When we pass back state values, these timings
can be disassociated from their usage, causing Compose to not register the states appropriately and not update
when the state value changed.

To address this, we've changed the return values of simple functions like `rememberConnectionState` to return
`State` objects instead of the values directly. This means that their reads will be more closely aligned with 
their usages and prevent issues with Compose not updating appropriately. 

To migrate, switch to using the `by` delegate syntax when declaring an object to hold the state:

```
val connectionState by rememberConnectionState()
```

In places where we return data objects to hold multiple values (such as `rememberRoomInfo`), we've kept the API
to return values, as these have been converted to be delegates to the state objects backing them.