This liquibase extensions cover cases not managed by liquibase-core library.

```xml
<dependency>
    <groupId>com.thedigitalstack.liquibase</groupId>
    <artifactId>liquibase-ext</artifactId>
    <version>1.0.0</version>
</dependency>
```

Compatible with Liquibase 4.x

In order to use them in liquibase files you have to add the following schema definition in the XML databasechangelog:

```xml
<?xml version="1.1" encoding="UTF-8"?>
<databaseChangeLog ...
    xmlns:nfalco79="http://www.liquibase.org/xml/ns/dbchangelog-ext/nfalco79"
    xsi:schemaLocation="...
        http://www.liquibase.org/xml/ns/dbchangelog-ext/nfalco79 liquibase/ext/nfalco79/dbchangelog-nfalco79.xsd">
```

## ChangeSet
### copyColumn

Some databases (for example Oracle 11g) don't allow alter table of a column of type VARCHAR to CLOB. The correct way to proceed is add a new column and then move data to the new column using custom copyColumn change.

Here are two use cases, the first moving from INTEGER to NUMERIC and the second from VARCHAR to CLOB:

```xml
<changeSet id="<jira-issue-id>-1" author="Jhon Doe">
    <!-- rename old column so we can create the new-one with desired name -->
    <renameColumn tableName="table1" oldColumnName="duration" newColumnName="durationtemp" />
    <!-- create new column with desired name and type -->
    <addColumn tableName="table1">
        <column name="duration" type="numeric(22, 8)" />
    </addColumn>
    <!-- copy data from old column to the new-one, the conversion is done internally using database specific function -->
    <nfalco79:copyColumn tableName="table1" fromName="durationtemp" fromType="INTEGER" toName="duration" toType="NUMERIC" />
    <!-- remove old column -->
    <dropColumn tableName="comfin_productmarketinfor_1oqv" columnName="durationtemp" />
</changeSet>

<changeSet author="<jira-issue-id>-1" id="Jhon Doe">
    <!-- rename old column so we can create the new-one with desired name -->
    <renameColumn tableName="table2" oldColumnName="specialization" newColumnName="specializationtmp" />
    <!-- create new column with desired name and type -->
    <addColumn tableName="table2">
        <column name="specialization" type="CLOB" />
    </addColumn>
    <!-- copy data from old column to the new one, the conversion is done internally using database specific function -->
    <nfalco79:copyColumn tableName="table2" fromName="specializationtmp" fromType="VARCHAR" toName="specialization" toType="CLOB" />
    <!-- remove old column -->
    <dropColumn tableName="table2" columnName="specializationtmp" />
</changeSet>
```

### resizeDataType

This change is similar to modifyDataType. It's used to resize a column if source and target data type is the same. This change is subject to the Oracle 11g check to deny resize of VARCHAR column greater than 4000 characters.

In this example metadata column size is increased.

```xml
<changeSet author="<jira-issue-id>-1" id="Jhon Doe">
	<createTable tableName="df_pdfacroform">
    	<column name="id" type="bigint">
    	    <constraints nullable="false" primaryKey="true" primaryKeyName="df_pdfacroform_pkey" />
	    </column>
    	<column name="metadata" type="CLOB(20480)" />
	    <column name="contentmimetype" type="varchar(255)" />
    	<column name="content" type="blob" />
	    <column name="tenant_id" type="varchar(31)" />
    	<column name="updateversion" type="integer" />
	</createTable>
</changeSet>

<changeSet author="<jira-issue-id>-2" id="Jhon Doe">
    <!-- resize metadata CLOB column from 20kb to 400kb -->
    <nfalco79:resizeDataType tableName="df_pdfacroform" columnName="metadata" newDataType="CLOB(409600)" oldDataType="CLOB" />
</changeSet>
```

## PreCondition
### dbVersion

This precondition could be used to verify a database version against a specific range.

In the example we would create a specific view using PL/SQL specific for Oracle 11g. In Oracle 10g syntax is not supported and in Oracle12c works in a standard way like other DBs

```xml
<changeSet author="<jira-issue-id>-1" id="Jhon Doe" dbms="oracle">
	<preConditions onFail="CONTINUE"> <!-- always CONTINUE -->
		<nfalco79:dbVersion minVersion="11" maxIncluded="true" maxVersion="12" maxIncluded="false" />
	</preConditions>
	<createView viewName="nfalco79_user_test">
		...
	</createView>
</changeSet>
nonNullConstraintExists
```

This precondition verify if a column accept NULL as value.

In the example we would create add a non null constraint only if not exists

```xml
<changeSet author="<jira-issue-id>-1" id="Jhon Doe" dbms="oracle">
	<preConditions onFail="MARK_RAN">
        <not>
            <nfalco79:nonNullConstraintExists tableName="table1" columnName="tenant_id" />
        </not>
	</preConditions>
    <addNotNullConstraint tableName="table1" columnName="tenant_id" defaultNullValue="N/A" columnDataType="VARCHAR" />
</changeSet>
```