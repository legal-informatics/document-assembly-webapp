///<reference path="../node_modules/typescript/lib/lib.d.ts"/>
/* import '../node_modules/zone.js/dist/zone.js'; */
/* import '../node_modules/reflect-metadata/Reflect.js'; */
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {AppModule} from './app.module';

platformBrowserDynamic().bootstrapModule(AppModule);
