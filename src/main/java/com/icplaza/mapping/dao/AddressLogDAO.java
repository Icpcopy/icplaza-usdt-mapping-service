package com.icplaza.mapping.dao;

import com.icplaza.mapping.model.AddressLogModel;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AddressLogDAO {
    String table = "address_log";

    @Insert("insert into address_log (icplaza,address,address_type,private_key,create_time) values (#{icplaza},#{address},#{addressType}, #{privateKey},#{createTime,jdbcType=TIMESTAMP})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AddressLogModel record);

    @Update("update address_log set trx=#{trx},usdt=#{usdt},last_scan=#{lastScan} where address=#{address}")
    boolean update(AddressLogModel addressLogModel);

    @Select("select * from address_log where icplaza=#{icplaza}")
    @Results({
            @Result(column = "private_key", property = "privateKey"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "address_type", property = "addressType")
    })
    List<AddressLogModel> selectModelsByICPlaza(String icplaza);


    @Select("select * from address_log where icplaza=#{icplaza} and address_type=#{addressType} limit 1")
    @Results({
            @Result(column = "private_key", property = "privateKey"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "address_type", property = "addressType")
    })
    AddressLogModel selectModelByICPlazaAndType(String icplaza, Integer addressType);

    @Select("select * from " + table + " where address=#{address} limit 1")
    @Results({
            @Result(column = "private_key", property = "privateKey"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "address_type", property = "addressType")
    })
    AddressLogModel selectModelByAddress(String address);


    @Select("select * from address_log order by id limit #{start}, #{pageSize}")
    @Results({
            @Result(column = "private_key", property = "privateKey"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "last_scan", property = "lastScan"),
            @Result(column = "address_type", property = "addressType")
    })
    List<AddressLogModel> selectAllModels(Integer start, Integer pageSize);

    @Select("select * from address_log where address_type=#{addressType}  order by id limit #{start}, #{pageSize}")
    @Results({
            @Result(column = "private_key", property = "privateKey"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "last_scan", property = "lastScan"),
            @Result(column = "address_type", property = "addressType")
    })
    List<AddressLogModel> selectModels(Integer start, Integer pageSize, Integer addressType);

    @Select("<script>select * from " + table + " where address_type=#{addressType} <if test='usdt!=null'>and usdt >= #{usdt}</if>  order by id limit #{start}, #{pageSize}</script>")
    @Results({
            @Result(column = "private_key", property = "privateKey"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "last_scan", property = "lastScan")
    })
    List<AddressLogModel> selectModelsWithCondition(Integer start, Integer pageSize, Double usdt, Integer addressType);


    //tron address total
    @Select("select count(*) from " + table)
    Integer total();

    //tron address total
    @Select("select count(*) from " + table + " where address_type=0")
    Integer rows();

    //bsc address total
    @Select("select count(*) from " + table + " where address_type=1")
    Integer rows2();

}
