# Development Standards

To maintain a professional workflow, this project follows industry-standard conventions.

## 🌿 Branching Strategy
We use a simplified **Git Flow** approach:
* `main`: Production-ready code only.
* `develop`: Integration branch for features.
* `feature/feature-name`: Specific tasks or new functionalities.
* `fix/bug-name`: Critical bug fixes.

**Example:** `feature/database-config` or `feature/user-auth`.

## 📝 Commit Messages
We follow **Conventional Commits** to keep a clean and readable history:

| Type | Purpose | Example |
| :--- | :--- | :--- |
| `feat` | A new feature | `feat: add postgres connection config` |
| `fix` | A bug fix | `fix: resolve null pointer in task service` |
| `docs` | Documentation changes | `docs: update setup instructions in readme` |
| `style` | Formatting, missing semi colons, etc. | `style: linting controller classes` |
| `refactor` | Code change that neither fixes a bug nor adds a feature | `refactor: simplify date logic` |
| `chore` | Updating build tasks, package manager configs, etc. | `chore: update maven dependencies` |

*Note: Use lowercase and be concise.*
