ALTER TABLE hanyang.resource
MODIFY COLUMN resource_url TEXT;

ALTER TABLE hanyang.question
MODIFY COLUMN content TEXT;

ALTER TABLE hanyang.answer
MODIFY COLUMN content TEXT;

ALTER TABLE hanyang.data_request
MODIFY COLUMN content TEXT;

ALTER TABLE hanyang.dataset
MODIFY COLUMN description TEXT;