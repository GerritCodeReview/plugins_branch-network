gerrit_plugin(
  name = 'branch-network',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  manifest_entries = [
    'Gerrit-PluginName: branch-network',
    'Gerrit-Module: com.googlesource.gerrit.plugins.branchnetwork.GitCommitCacheModule',
    'Gerrit-HttpModule: com.googlesource.gerrit.plugins.branchnetwork.NetworkGraphModule'
  ],
  deps = [
    '//lib:gson',
  ],
)

