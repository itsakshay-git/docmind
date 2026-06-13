# Shared Types

Backend-facing DTOs are split by product feature and re-exported through `index.ts`.

Prefer imports from the barrel:

```ts
import type { Notebook, StudioArtifact } from "../../shared/types";
```

`api.ts` remains as a compatibility barrel for older imports, but new code should use `shared/types`.
