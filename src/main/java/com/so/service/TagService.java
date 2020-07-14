package com.so.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.so.common.Utils;
import com.so.exception.BusinessException;
import com.so.jpa.entity.Tag;
import com.so.jpa.repository.TagRepository;
import com.so.mybatis.mapper.TagMapper;
import com.so.mybatis.model.TagVO;

@Service
public class TagService {

	@Resource
	private TagMapper tagMapper;
	
	@Autowired
	private TagRepository tagRepository;

	public List<TagVO> getTags(Map<String, Object> param) {
		return tagMapper.selectTags(param);
	}

	public Tag addTag(Map<String, Object> param) throws BusinessException {
		String tagName = (String) param.get("tag");
		if(tagRepository.existsByTag(tagName)) {
			throw new BusinessException("Tag name \""+tagName+"\" already exists !");
		}
		return tagRepository.save(new Tag(tagName));
	}

	@Transactional
	public void removeTag(List<Integer> tagIds) {
		tagRepository.deleteTagByIds(Utils.toListLong(tagIds));
	}
}
