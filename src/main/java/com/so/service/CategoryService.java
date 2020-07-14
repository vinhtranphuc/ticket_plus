package com.so.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.so.common.Const;
import com.so.common.FileUtils;
import com.so.exception.BusinessException;
import com.so.jpa.entity.Category;
import com.so.jpa.repository.CategoryRepository;
import com.so.jpa.repository.PostRepository;
import com.so.mybatis.mapper.CategoryMapper;
import com.so.mybatis.model.CategoryVO;
import com.so.payload.CategoryRequest;

@Service
public class CategoryService {

	@Resource
	private CategoryMapper categoryMapper;
	
	@Autowired private CategoryRepository categoryRepository;
	
	@Autowired private PostRepository<?> postRepository;
	
	public List<CategoryVO> getCategories(Map<String, Object> param) {
		return categoryMapper.selectCategories(param);
	}
	
	public Boolean isExistsCategory(String category) {
		if(StringUtils.isEmpty(category)) 
			return false;
		return categoryRepository.existsByCategory(category);
	}
	
	public Category saveCategory(CategoryRequest categoryRequest) throws BusinessException {
		
		if(categoryRepository.existsByCategory(categoryRequest.category)) {
			throw new BusinessException("Category name \""+categoryRequest.category+"\" already exists !");
		}
		
		String categoryImg = FileUtils.saveBase64Image(categoryRequest.categoryImg, Const.IMG_CATEGORY_PATH,categoryRequest.category);
		Category category = new Category(categoryRequest.category, categoryImg);
		
		return categoryRepository.save(category);
	}

	public void removeCategory(Long categoryId) throws BusinessException {
		String categoryImg = categoryRepository.findCategoryImgById(categoryId);
		if(!postRepository.existsByCategoryId(categoryId)) {
			categoryRepository.deleteById(categoryId);
			FileUtils.deleteImage(Const.IMG_CATEGORY_PATH,categoryImg);
			return;
		}
		throw new BusinessException("This category are being used in other posts !");
	}
}
