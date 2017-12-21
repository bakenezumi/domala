select * from person
where
/*%for name: names */
name like /* name + "%" */'hoge%'
  /*%if name_has_next */
/*# "or" */
  /*%end */
/*%end*/
