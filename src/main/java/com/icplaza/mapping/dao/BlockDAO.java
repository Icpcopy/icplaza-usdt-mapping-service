package com.icplaza.mapping.dao;

import com.icplaza.mapping.model.BlockModel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BlockDAO {
    String table = "block";

    @Insert("insert into " + table + " (height,code) values(#{height},#{code})")
    int add(BlockModel model);

    /**
     * 仅更新更高区块
     */
    @Update("update " + table + " set height=#{height} where code = #{code}")
    boolean update(BlockModel model);

    @Select("select * from " + table + " where code=#{code}")
    BlockModel getBlock(Integer code);

    @Select("select height from " + table + " where code = #{code}")
    Long getHeight(Integer code);
}
