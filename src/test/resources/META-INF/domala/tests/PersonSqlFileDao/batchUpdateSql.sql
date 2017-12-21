update person set
  name = /* persons.name */'hoge',
  age = /* persons.age */0,
  city = /* persons.address.city */'hoge',
  street = /* persons.address.street */'hoge',
  department_id = /* 2 */0,
  version = version + 1
where
  id = /* persons.id */0 and
  version = /* persons.version */0
