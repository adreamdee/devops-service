package script.db
databaseChangeLog(logicalFilePath: 'dba/devops_cluster_resource.groovy') {
    changeSet(author: 'scp', id: '2019-10-23-create-table') {
        createTable(tableName: "devops_cluster_resource", remarks: 'cluster resource') {
            column(name: 'id', type: 'BIGINT UNSIGNED', remarks: '主键，ID', autoIncrement: true) {
                constraints(primaryKey: true)
            }
            column(name: 'name', type: 'VARCHAR(64)', remarks: '资源名字')
            column(name: 'code', type: 'VARCHAR(64)', remarks: '资源编码')
            column(name: 'type', type: 'VARCHAR(15)', remarks: '资源类型')
            column(name: 'cluster_id', type: 'BIGINT UNSIGNED', remarks: '集群id')
            column(name: 'instance_id', type: 'BIGINT UNSIGNED', remarks: '实例id')
            column(name: 'config_id', type: 'BIGINT UNSIGNED', remarks: '配置id')
            column(name: "object_version_number", type: "BIGINT UNSIGNED", defaultValue: "1")
            column(name: "created_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "creation_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
            column(name: "last_updated_by", type: "BIGINT UNSIGNED", defaultValue: "0")
            column(name: "last_update_date", type: "DATETIME", defaultValueComputed: "CURRENT_TIMESTAMP")
        }
    }
}