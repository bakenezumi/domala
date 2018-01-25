select * from person
where
/*%if id != null*/
  id = /*id*/0
/*%elseif departmentId != null */
  and
  department_id = /*departmentId */0
/*%else */
  and
  department_id is null
/*%end*/
