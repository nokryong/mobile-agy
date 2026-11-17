const path = require('node:path');
// Match the native stable logical URL while serving the actual deployment bytes.
function iconAsset(web, pathname) {
  return path.resolve(web, '.' + pathname);
}
module.exports = {iconAsset};
