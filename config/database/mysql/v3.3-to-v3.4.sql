ALTER TABLE `LABEL` 
    ADD COLUMN iconName VARCHAR(50) NOT NULL;

UPDATE LABEL SET iconName = 'glyphicon glyphicon-tag';
