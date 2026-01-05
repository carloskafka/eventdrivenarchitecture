# CI / CD for eventdrivenarchitecture

This document explains how the GitHub Actions workflow `.github/workflows/ci-cd.yml` works, how to trigger it, and how to validate CI and CD runs.

...existing content...

Deploy to GitHub Packages (note about configuration)

To avoid requiring a `distributionManagement` section inside the parent POM, the CI uses Maven's `-DaltDeploymentRepository` to point to GitHub Packages during the `deploy` step. The workflow sets the alt deployment URL using the repository context:

```
-DaltDeploymentRepository=github::default::https://maven.pkg.github.com/$GITHUB_REPOSITORY
```

This makes Maven send artifacts to your GitHub Packages repository for the current repository (e.g. `OWNER/REPO`). The workflow also passes credentials to Maven using `actions/setup-java` server configuration with `server-id: github` and `${{ secrets.GITHUB_TOKEN }}`.

If you prefer to publish using `distributionManagement` in the POM, you can add a section like:

```xml
<distributionManagement>
  <repository>
    <id>github</id>
    <name>GitHub OWNER Apache Maven Packages</name>
    <url>https://maven.pkg.github.com/OWNER/REPO</url>
  </repository>
</distributionManagement>
```

Replace `OWNER/REPO` accordingly or keep the CI altDeploymentRepository approach which works without modifying POMs.

Troubleshooting

- If deploy fails with "repository element was not specified", ensure the CI uses `-DaltDeploymentRepository` (our workflow does) or add `distributionManagement` to the POM.
- Ensure `GITHUB_TOKEN` has `packages: write` permission (the workflow sets permissions: packages: write). In some cases GitHub's automatically provided `GITHUB_TOKEN` may be rejected by GitHub Packages for package deployment (401). In that case create a Personal Access Token (PAT) with the following scopes: `repo` (or `public_repo` for public repos) and `packages`, and add it to the repository secrets as `PACKAGES_TOKEN`.
- For third-party registries, configure secrets and server credentials accordingly.

```powershell
# Example: run locally (you'll need a token with repo:packages permissions)
mvn -B -ntp -T 1C clean deploy -DskipTests -Dgpg.skip=true -DaltDeploymentRepository=github::default::https://maven.pkg.github.com/OWNER/REPO -DserverId=github -Dusername=USERNAME -Dpassword=PERSONAL_ACCESS_TOKEN
```

If you want, I can also:
- Add a `distributionManagement` snippet to the parent POM instead (I can add a templated comment explaining how to set OWNER/REPO).
- Configure the workflow to publish only selected modules (instead of whole reactor).

Tell me which option you prefer and I will implement it.
