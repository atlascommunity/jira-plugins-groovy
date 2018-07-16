const path = require('path');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const WrmPlugin = require('atlassian-webresource-webpack-plugin');

const devMode = process.env.NODE_ENV !== 'production';
const OUTPUT_DIR = '../src/main/resources/';

module.exports = {
    entry: {
        main: ['./config/polyfills', './src/app-main/index.js'],
        workflow: ['./config/polyfills', './src/app-workflow/index.js'],
    },
    output: {
        path: path.resolve(OUTPUT_DIR, "build"),
        filename: 'ru/mail/jira/plugins/groovy/js/[name].js',
        chunkFilename: 'ru/mail/jira/plugins/groovy/js/[name].chunk.js',
        publicPath: "/assets/",
        jsonpFunction: 'mailruGroovyWebpackJsonp',
        sourceMapFilename: '[file].smap'
    },
    plugins: [
        new MiniCssExtractPlugin({
            filename: "[name].css"
        }),
        new WrmPlugin({
            pluginKey: 'ru.mail.jira.plugins.groovy',
            amdProvider: 'jira.webresources:almond',
            xmlDescriptors: path.resolve(OUTPUT_DIR, 'META-INF', 'plugin-descriptors', 'wr-defs.xml'),
            contextMap: {
                main: ['ru.mail.jira.plugins.groovy.main'],
                workflow: ['ru.mail.jira.plugins.groovy.workflow'],
            },
            providedDependencies: {
                'jquery': {
                    dependency: 'com.atlassian.plugins.jquery:jquery',
                    import: {
                        amd: 'jquery'
                    }
                },
                'AJS': {
                    dependency: 'com.atlassian.auiplugin:aui-core',
                    import: {
                        var: 'AJS'
                    }
                }
            }
        })
    ],
    externals: {
        'external-i18n': 'require(\'/mailru/groovy/i18n-react\')',
        extDefine: 'define',
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                enforce: 'pre',
                loader: 'eslint-loader'
            },
            {
                oneOf: [
                    {
                        test: /\.js$/,
                        exclude: /node_modules/,
                        loader: 'babel-loader',
                        options: {
                            presets: [
                                require('@babel/preset-env'),
                                require('@babel/preset-react'),
                                require('@babel/preset-flow'),
                            ],
                            plugins: [
                                'flow-react-proptypes',
                                require('@babel/plugin-transform-destructuring'),
                                require('@babel/plugin-proposal-class-properties'),
                                require('@babel/plugin-proposal-object-rest-spread')
                            ]
                        }
                    },
                    {
                        test: /\.less$/,
                        use: [
                            devMode ? 'style-loader' : MiniCssExtractPlugin.loader,
                            'css-loader',
                            'less-loader'
                        ]
                    },
                    {
                        loader: require.resolve('file-loader'),
                        exclude: [/\.js$/, /\.html$/, /\.json$/],
                        options: {
                            name: 'static/media/[name].[hash:8].[ext]',
                        },
                    },
                ]
            }
        ]
    }
};
