load("//tools/bzl:plugin.bzl", "gerrit_plugin")

gerrit_plugin(
    name = "branch-network",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/**/*"]),
    manifest_entries = [
        "Gerrit-PluginName: branch-network",
        "Gerrit-Module: com.googlesource.gerrit.plugins.branchnetwork.GitCommitCacheModule",
        "Gerrit-HttpModule: com.googlesource.gerrit.plugins.branchnetwork.NetworkGraphModule",
    ],
)
