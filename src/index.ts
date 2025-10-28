import { registerPlugin } from '@capacitor/core';

import type { vestvalidatorPlugin } from './definitions';

const vestvalidator = registerPlugin<vestvalidatorPlugin>('vestvalidator', {
  web: () => import('./web').then((m) => new m.vestvalidatorWeb()),
});

export * from './definitions';
export { vestvalidator };
