import { WebPlugin } from '@capacitor/core';

import type { 
  vestvalidatorPlugin,
  HasVestOptions

} from './definitions';

export class vestvalidatorWeb extends WebPlugin implements vestvalidatorPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
  async checkHasVest(options: HasVestOptions): Promise<{ hasVest: boolean }> {  
    console.log('CHECK HAS VEST (web stub)', options);
    return { hasVest: false };
  }
}
