import { SystemModule } from '../system-module/system-module';

export interface SystemModuleGroup {
  name: string;
  systemModules: SystemModule[];
}
