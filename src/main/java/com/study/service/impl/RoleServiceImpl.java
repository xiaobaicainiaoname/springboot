package com.study.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.study.mapper.ResourcesMapper;
import com.study.mapper.RoleMapper;
import com.study.mapper.RoleResourcesMapper;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.study.model.Resources;
import com.study.model.Role;
import com.study.model.RoleResources;
import com.study.service.RoleService;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;
import tk.mybatis.mapper.util.StringUtil;

@Service("roleService")
public class RoleServiceImpl extends BaseService<Role> implements RoleService{

    @Resource
    private RoleMapper roleMapper;
    @Resource
    private RoleResourcesMapper roleResourcesMapper;

    @Override
    public List<Role> queryRoleListWithSelected(Integer uid) {
        return roleMapper.queryRoleListWithSelected(uid);
    }

    @Override
    public PageInfo<Role> selectByPage(Role role, int start, int length) {
        int page = start/length+1;
        Example example = new Example(Role.class);
        //分页查询
        PageHelper.startPage(page, length);
        List<Role> rolesList = selectByExample(example);
        return new PageInfo<>(rolesList);
    }

    @Override
    @Transactional(propagation= Propagation.REQUIRED,readOnly=false,rollbackFor={Exception.class})
    public void delRole(Integer roleid) {
        //删除角色
        mapper.deleteByPrimaryKey(roleid);
        //删除角色资源
        Example example = new Example(RoleResources.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("roleid",roleid);
        roleResourcesMapper.deleteByExample(example);

    }
    @Autowired
    private ResourcesMapper resourcesMapper;
    //查询返回数据
	@Override
	public PageInfo<Role> modifyRoleAndPermission(RoleResources roleResources, int start, int pageSize) {
		//查询模块信息
		Example example = new Example(Resources.class);
		Criteria criteria = example.createCriteria();
		criteria.andEqualTo("parentid", 0);
		Page<Object> startPage = PageHelper.startPage(start, pageSize);
		List<Resources> models = resourcesMapper.selectByExample(example);
		List<Object> resourcesRoles = new ArrayList<>();
		//按模块封装数据[{{x:x,y:y,n:n},{}},{{},{}}]这样是乱序=========问题怎样防止乱序=使用td1 td2 tdn来保证顺序
		//采用多层级封装[]
		//x是角色，y是资源--[{y:[x1,x2],n:n},{y:[x1,x2]}]
		//[th:{y:[x1,x2],n:n},tr:{y:[x1,x2]}]
		for (Resources p : models) {
			//第一行表头数据，根据模块查所有资源
			Example example2 = new Example(Resources.class);
			Criteria criteria2 = example2.createCriteria();
			criteria2.andEqualTo("id", p.getId());example2.orderBy("id asc");
			List<Resources> resources = resourcesMapper.selectByExample(example2);
			
			Map<String, Object> firstRow = new HashMap<>();
			Map<String, Object> td = new HashMap<>();
			td.put("td1", "应用-模块");td.put("td2", "角色名称");
			firstRow.put("th",td );
			firstRow.put("resourceid", 0);
				
			
		}
		return null;
	}
	@Test
	public void name() throws JsonProcessingException {
			
		Map<String, Object> rows = new HashMap<>();
		//第一行数据
		Map<String, Object> td = new HashMap<>();
		td.put("td1", "应用-模块");
		List<Object> list = new ArrayList<>();
		Map<String, Object> thTds = new HashMap<>();
		thTds.put("td2", new RoleResources(0, 0, "用户角色"));
		for (int i=3; i<5 ; i++) {
			thTds.put("td"+i, new RoleResources(i, 0, "读权限"+i));
		}
		list.add(thTds);
		td.put("tds", list);
		rows.put("th",td );//第一行
		////////////////第二行/////////////////////////////////
		Map<String, Object> tr = new HashMap<>();
		tr.put("td1", "权限-用户模块");
		Map<String, Object> trTds = new HashMap<>();
		trTds.put("td2", new RoleResources(0, 2, "平台管理员"));
		for (int i = 3; i < 5; i++) {
			trTds.put("td"+i, new RoleResources(1, i, "true") );
		}
		tr.put("tds", trTds);
		rows.put("tr", tr);
		/////////////////////////////////////////////////
		ObjectMapper objectMapper = new ObjectMapper();
		String string = objectMapper.writeValueAsString(rows);
		System.out.println(string);
	}
	
	@Test//使用这个再封装分页信息
	public void name2() throws JsonProcessingException {
			
		Map<String, Object> bigRows = new HashMap<>();//总共两大行
//		//第一行数据--第一列
//		List<Object> list = new ArrayList<>();
//		Map<String, Object> tr1td1 = new HashMap<>();
//		tr1td1.put("td1", "应用-模块");
//		//第二行数据--第一列
//		Map<String, Object> tr2td1 = new HashMap<>();
//		tr2td1.put("td1", "权限-用户模块");
		
		////循环得到剩下的数据(x,y)x是资源权限,y是角色==先查询出模块的所有资源遍历
		//也要查询出应用的所有角色y
		//=========注意有加一操作
		///一行一行的遍历比较好封装数据 
		List<Object> rows1 = new ArrayList<>();//第一行即tds
		List<Object> rowss = new ArrayList<>();
		for (int y=1; y<4+1 ; y++) {//(m,n)是真正的id,从数据库中取出来的,外层是角色，内层是权限第一行开始
			//循环一行，一行得到数据
			//小行
//			List<Object> row = new ArrayList<>();
			Map<String, Object> row = new HashMap<>();//使用key进行排序
			for (int x = 2; x < 5; x++) {//从第二列开始
				if(y==1) {//第一行的数据,与角色无关的
					Map<String, Object> column = new HashMap<>();
					if(x==2) {//第二列第一行与任何数据无关
						column.put("td2", new RoleResources(0, 0, "角色名称"));
					}else {
						column.put("td2", new RoleResources(x, 0, "读权限"+x));
					}//将每一列添加到行
//					row.add(column);
					row.put("td"+x, column);
				}else {//剩下行的资源与角色
					Map<String, Object> column = new HashMap<>();
					if(x==2) {//第二列数据除了第一行，只与角色相关
						column.put("td2", new RoleResources(0, y, "用户角色"));
					}else {
						column.put("td2", new RoleResources(x, 0, "true"));
					}
//					row.add(column);
					row.put("td"+x, column);
				}
			}//将每一行添加到集合
			if(y==1) {
				rows1.add(row);
			}else {
				rowss.add(row);
			}
		}
		//封装两行数据
		Map<String, Object> th = new HashMap<>();
		th.put("td1", "应用-模块");
		th.put("tds", rows1);
		
		Map<String, Object> tr = new HashMap<>();
		tr.put("td1", "权限-用户模块");
		tr.put("tds", rowss);
		bigRows.put("th", th);
		bigRows.put("tr", tr);
		/////////////////////////////////////////////////
		ObjectMapper objectMapper = new ObjectMapper();
		String string = objectMapper.writeValueAsString(bigRows);
		System.out.println(string);
	}
	//
/**
 * #得到模块id=100
#得到工程id=2
#查询得到所有的权限id   SELECT resources.id FROM resources WHERE parentId=100 ORDER BY id ASC
#查询所有的角色和所有的模块下的权限
#所有mybatis生产查询代码
SELECT
	role.id,role.roleName,
	使用循环生产以下代码
	(CASE WHEN EXISTS (SELECT 1 FROM role_resources rr where rr.roleid=role.id AND rr.resourcesId=101 ) OR role.parentId=0 THEN 'true' ELSE 'false' END) AS '101',
	(CASE WHEN EXISTS (SELECT 1 FROM role_resources rr where rr.roleid=role.id AND rr.resourcesId=102 ) OR role.parentId=0 THEN 'true' ELSE 'false' END) AS '102',
	(CASE WHEN EXISTS (SELECT 1 FROM role_resources rr where rr.roleid=role.id AND rr.resourcesId=103 ) OR role.parentId=0 THEN 'true' ELSE 'false' END) AS '103'
	
FROM
	role
WHERE
	role.projectId = 2
	
	
	SELECT
	role.id,role.roleName,
	(CASE WHEN EXISTS (SELECT 1 FROM role_resources rr where rr.roleid=role.id AND rr.resourcesId=101 ) OR role.parentId=0 THEN '101' ELSE 'null' END) AS '101',
	(CASE WHEN EXISTS (SELECT 1 FROM role_resources rr where rr.roleid=role.id AND rr.resourcesId=102 ) OR role.parentId=0 THEN '102' ELSE 'null' END) AS '102',
	(CASE WHEN EXISTS (SELECT 1 FROM role_resources rr where rr.roleid=role.id AND rr.resourcesId=103 ) OR role.parentId=0 THEN '103' ELSE 'null' END) AS '103'
	
FROM
	role
WHERE
	role.projectId = 2
 */
//	for (int x=2; x<4+1 ; x++) {//(m,n)是真正的id,从数据库中取出来的,外层是权限，内层是角色
//		//循环一列，一列的得到数据
//		for (int y = 3; y < 5; y++) {
//			if(x==2) {//第一列的数据,与权限无关的
//				
//				Map<String, Object> thTds = new HashMap<>();
//				thTds.put("td2", new RoleResources(0, 0, "用户角色"));
//			}else {
//				
//			}
//			
//			
//			
//			
//			
//			
//			trTds.put("td"+i, new RoleResources(1, i, "true") );
//		}
//		
//		
//		thTds.put("td"+x, new RoleResources(i, 0, "读权限"+x));
//		if(x>2) {
//			
//		}
//	}
//	Map<String, Object> trTds = new HashMap<>();
//	trTds.put("td2", new RoleResources(0, 2, "平台管理员"));
}
