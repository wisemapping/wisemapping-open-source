# Changelog

All notable changes to the Wisemapping Frontend project are documented in this file.

## Week of September 24 - October 1, 2025

### 🚀 Major Features & Enhancements

#### Theme System Overhaul
- **Dark Mode Support**: Added comprehensive dark mode support throughout the application
- **Theme Variants**: Implemented theme variant system with mandatory variant parameters for consistent theme resolution
- **New Themes**: Added Ocean theme variant with improved color schemes
- **Theme Styling**: Moved theme styles to JSON format for better maintainability and consistency
- **Font Color "None" Option**: Fixed font color "none" option to work correctly in both light and dark modes

#### Social Media Integration
- **Facebook Support**: Enabled Facebook social media integration for sharing and collaboration features

#### UI/UX Improvements
- **Look and Feel**: Significant improvements to overall application appearance and user experience
- **Relationship Styling**: Added enhanced styling options for mindmap relationships
- **Image Support**: Completed emoji-based image support implementation
- **Background Colors**: Added support for custom background colors in themes

### 🔧 Technical Improvements

#### Code Modernization
- **jQuery Removal**: Completed migration away from jQuery dependency
- **JavaScript Migration**: Completed .js to TypeScript migration across the codebase
- **Type Safety**: Enhanced TypeScript type safety for theme-related operations
- **Code Cleanup**: Removed unused imports, variables, and deprecated logic

#### Testing & Quality Assurance
- **Test Coverage**: Added multiple new test cases for improved code coverage
- **Linting**: Fixed various linting issues across the codebase
- **Compilation**: Resolved TypeScript compilation errors
- **Test Fixes**: Fixed and re-enabled previously failing tests

### 🐛 Bug Fixes

#### Rendering & Display
- **Render Bugs**: Fixed several rendering issues that affected mindmap display
- **Theme Selector**: Fixed theme selector background display issues
- **Image Rendering**: Fixed image rendering and event handling issues
- **Color Resolution**: Fixed color resolution issues in various theme modes

#### Analytics & Tracking
- **Analytics Events**: Improved analytics event tracking implementation
- **Topic Tracking**: Removed tracking for selected topic to improve performance and privacy

### 📦 Dependencies & Version Updates

#### Version Management
- **Version 6**: Updated application to version 6.0
- **Copyright**: Updated copyright information across the project
- **Package Updates**: Various package.json updates for consistency

### 🔄 Refactoring & Architecture

#### Theme Architecture
- **ThemeFactory**: Updated ThemeFactory.create() and createById() to require variant parameters
- **Theme Implementations**: Refactored all theme implementations (PrismTheme, EnhancedPrismTheme, DarkPrismTheme, ClassicTheme, RobotTheme)
- **Topic Styling**: Updated Topic class methods for better theme integration
- **Component Updates**: Updated all components to properly handle theme variants

#### Component Improvements
- **StandaloneActionDispatcher**: Enhanced color change methods to use Designer's current variant
- **NodePropertyBuilder**: Updated to pass variant when getting color values
- **MindplotWebComponent**: Improved theme resolution and variant handling

### 📁 Files Modified

#### Core Theme Files
- `packages/mindplot/src/components/theme/Theme.ts`
- `packages/mindplot/src/components/theme/ThemeFactory.ts`
- `packages/mindplot/src/components/theme/DefaultTheme.ts`
- `packages/mindplot/src/components/theme/PrismTheme.ts`
- `packages/mindplot/src/components/theme/EnhancedPrismTheme.ts`
- `packages/mindplot/src/components/theme/DarkPrismTheme.ts`
- `packages/mindplot/src/components/theme/ClassicTheme.ts`
- `packages/mindplot/src/components/theme/RobotTheme.ts`

#### Component Updates
- `packages/mindplot/src/components/Topic.ts`
- `packages/mindplot/src/components/StandaloneActionDispatcher.ts`
- `packages/mindplot/src/components/DesignerActionRunner.ts`
- `packages/mindplot/src/components/MainTopic.ts`
- `packages/mindplot/src/components/ImageEmoji.ts`
- `packages/mindplot/src/components/ImageEmojiFeature.ts`
- `packages/mindplot/src/components/ConnectionLine.ts`

#### Editor Components
- `packages/editor/src/classes/model/node-property-builder/index.ts`
- Multiple editor component files updated for theme consistency

### 🎯 Impact Summary

This week's changes represent a major milestone in the Wisemapping Frontend project:

1. **Enhanced User Experience**: Dark mode support and improved theming provide users with better visual customization options
2. **Better Performance**: Removal of jQuery and code cleanup improve application performance
3. **Improved Maintainability**: JSON-based theme configuration and better type safety make the codebase more maintainable
4. **Social Integration**: Facebook support expands collaboration capabilities
5. **Quality Assurance**: Comprehensive testing improvements ensure more reliable functionality

### 🔮 Future Considerations

The theme system overhaul and dark mode implementation provide a solid foundation for:
- Additional theme variants and customization options
- Enhanced accessibility features
- Improved mobile responsiveness
- Advanced collaboration features

---

*This changelog is automatically generated based on git commit history and may be updated as new changes are merged.*
