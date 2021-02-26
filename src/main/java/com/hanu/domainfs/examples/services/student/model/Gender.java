package com.hanu.domainfs.examples.services.student.model;

import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAttr.Type;

/**
 * @overview Represents the gender of a person.
 *
 * @author Duc Minh Le (ducmle)
 */
public enum Gender {
  Male,
  Female,
  //Others
  ;
  
  @DAttr(name="name", type=Type.String, id=true, length=10)
  public String getName() {
    return name();
  }
}
