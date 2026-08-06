const fs = require('fs');
const path = require('path');
const { DefaultArtifactClient } = require('@actions/artifact');

async function run() {
  const workspace = process.env.GITHUB_WORKSPACE || process.cwd();
  const pluginDir = path.join(workspace, 'plugin');
  const client = new DefaultArtifactClient();

  const plugins = fs.readdirSync(pluginDir, { withFileTypes: true })
    .filter((entry) => entry.isDirectory())
    .map((entry) => entry.name);

  if (plugins.length === 0) {
    throw new Error('No plugins found');
  }

  for (const plugin of plugins) {
    const zip = path.join(pluginDir, plugin, 'build', 'outputs', `${plugin}.zip`);
    if (!fs.existsSync(zip)) {
      throw new Error(`Missing build output: ${zip}`);
    }

    console.log(`Uploading ${plugin}`);
    const { id, size } = await client.uploadArtifact(plugin, [zip], workspace);
    console.log(`Uploaded ${plugin} (id: ${id}, bytes: ${size})`);
  }
}

run().catch((error) => {
  console.error(error);
  process.exit(1);
});
