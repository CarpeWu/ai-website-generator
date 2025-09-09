/**
 * API接口统一导出
 * 集中管理所有API控制器
 */
 
// API 更新时间：
// API 唯一标识：
import * as userController from './userController'
import * as chatHistoryController from './chatHistoryController'
import * as appController from './appController'
import * as staticResourceController from './staticResourceController'
import * as healthController from './healthController'

export default {
  userController,
  chatHistoryController,
  appController,
  staticResourceController,
  healthController,
}
