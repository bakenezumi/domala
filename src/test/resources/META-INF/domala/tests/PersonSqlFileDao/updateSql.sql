update person set
  name = /* entity.name */'hoge',
  age = /* entity.age */0,
  city = /* entity2.address.city */'hoge',
  street = /* entity2.address.street */'hoge',
  department_id = /* 2 */0,
  version = version + 1
where
  id = /* entity.id */0 and
  version = /* version */0
