package ustc.sse.yyx.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jdk.jfr.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ustc.sse.yyx.common.utils.PageUtils;
import ustc.sse.yyx.common.utils.Query;

import ustc.sse.yyx.product.dao.BrandDao;
import ustc.sse.yyx.product.dao.CategoryBrandRelationDao;
import ustc.sse.yyx.product.dao.CategoryDao;
import ustc.sse.yyx.product.entity.BrandEntity;
import ustc.sse.yyx.product.entity.CategoryBrandRelationEntity;
import ustc.sse.yyx.product.entity.CategoryEntity;
import ustc.sse.yyx.product.service.BrandService;
import ustc.sse.yyx.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    private final BrandDao brandDao;
    private final CategoryDao categoryDao;
    private final CategoryBrandRelationDao categoryBrandRelationDao;

    @Autowired
    private BrandService brandService;

    @Autowired
    public CategoryBrandRelationServiceImpl(BrandDao brandDao,
                                            CategoryDao categoryDao,
                                            CategoryBrandRelationDao categoryBrandRelationDao) {
        this.brandDao = brandDao;
        this.categoryDao = categoryDao;
        this.categoryBrandRelationDao = categoryBrandRelationDao;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetails(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        // 查询详细名字
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        this.update(categoryBrandRelationEntity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        this.baseMapper.updateCategory(catId, name);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> categoryBrandRelationEntities =
                categoryBrandRelationDao.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        return categoryBrandRelationEntities.stream().map(categoryBrandRelationEntity -> {
            Long brandId = categoryBrandRelationEntity.getBrandId();
            return brandService.getById(brandId);
        }).collect(Collectors.toList());
    }

}