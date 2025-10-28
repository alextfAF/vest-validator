import { WebPlugin } from '@capacitor/core';

import type { vestvalidatorPlugin } from './definitions';

export class vestvalidatorWeb extends WebPlugin implements vestvalidatorPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
