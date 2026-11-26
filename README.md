# DayFunStats 2.0

## Build locally

Requires JDK 21 and Maven:

```bash
mvn clean package
```

The compiled plugin will be in `target/`.

## Build with GitHub Actions

1. Create a GitHub repository for this plugin.
2. Upload the **contents** of this project folder to the repository root. The `.github/workflows/build.yml` file must therefore be at `.github/workflows/build.yml` in the repository.
3. Commit/push to `main` or `master`, or open the repository's **Actions** tab and run **Build DayFunStats** with **Run workflow**.
4. Open the completed workflow run and download the `DayFunStats-2.0` artifact under **Artifacts**. It contains the compiled JAR.

The workflow uses Java 21 and Maven and runs `mvn clean package` on GitHub's runner. It does not deploy the plugin to a server automatically.

## Server dependencies / integrations

- Paper 1.21.4 or a compatible version.
- PlaceholderAPI for the configured balance and rank placeholders.
- Vault with EssentialsX Economy registered as its economy provider, plus the PlaceholderAPI Vault expansion, for `%vault_eco_balance_formatted%`.
- LuckPerms and its PlaceholderAPI expansion for `%luckperms_primary_group_name%`.

No separate gems/points currency is used by this source.
