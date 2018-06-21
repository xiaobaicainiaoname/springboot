package com.study.model;

import javax.persistence.*;

import java.io.Serializable;

@Table(name = "role_resources")
public class RoleResources implements Serializable{
    private static final long serialVersionUID = -8559867942708057891L;
    
    @Id
    @Column(name = "resourcesId")
    private Integer resourcesid;
    
    @Id
    @Column(name = "roleId")
    private Integer roleid;
    
    @Transient
    private String relation;
    
    public RoleResources() {
		super();
	}
    
	public RoleResources( Integer resourcesid,Integer roleid, String relation) {
		super();
		this.roleid = roleid;
		this.resourcesid = resourcesid;
		this.relation = relation;
	}



	public Integer getRoleid() {
		return roleid;
	}

	public void setRoleid(Integer roleid) {
		this.roleid = roleid;
	}

	public Integer getResourcesid() {
		return resourcesid;
	}

	public void setResourcesid(Integer resourcesid) {
		this.resourcesid = resourcesid;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	
}