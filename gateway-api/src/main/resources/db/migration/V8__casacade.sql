alter table hanyang.resource drop foreign key fk_dataset;

alter table hanyang.resource
add constraint resource_ibfk_1
foreign key (dataset_id) references hanyang.dataset(dataset_id)
on delete cascade;

alter table hanyang.scrap drop foreign key scrap_ibfk_2;

alter table hanyang.scrap
add constraint scrap_ibfk_1
foreign key (dataset_id) references hanyang.dataset(dataset_id)
on delete cascade;

alter table hanyang.download drop foreign key download_ibfk_1;
alter table hanyang.download drop foreign key download_ibfk_2;

alter table hanyang.download
add constraint download_ibfk_1
foreign key (dataset_id) references hanyang.dataset(dataset_id)
on delete cascade;