// AES加密工具类
import CryptoJS from 'crypto-js';

/**
 * AES加密配置
 */
const AES_CONFIG = {
  // 密钥，需要与后端保持一致
  SECRET_KEY: 'shuzhi_database_master',
  // 偏移量，需要与后端保持一致
  IV: '1234567890123456'
};

/**
 * AES加密
 * @param {string} text - 待加密的文本
 * @returns {string} 加密后的Base64字符串
 */
export const encryptAES = (text) => {
  try {
    const key = CryptoJS.enc.Utf8.parse(AES_CONFIG.SECRET_KEY);
    const iv = CryptoJS.enc.Utf8.parse(AES_CONFIG.IV);
    
    // 使用CBC模式，PKCS7填充
    const encrypted = CryptoJS.AES.encrypt(text, key, {
      iv: iv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    });
    
    // 返回Base64编码的密文
    return encrypted.toString();
  } catch (error) {
    console.error('AES加密失败:', error);
    throw error;
  }
};

/**
 * AES解密
 * @param {string} ciphertext - 待解密的Base64字符串
 * @returns {string} 解密后的文本
 */
export const decryptAES = (ciphertext) => {
  try {
    const key = CryptoJS.enc.Utf8.parse(AES_CONFIG.SECRET_KEY);
    const iv = CryptoJS.enc.Utf8.parse(AES_CONFIG.IV);
    
    const decrypted = CryptoJS.AES.decrypt(ciphertext, key, {
      iv: iv,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    });
    
    // 返回UTF8编码的明文
    return decrypted.toString(CryptoJS.enc.Utf8);
  } catch (error) {
    console.error('AES解密失败:', error);
    throw error;
  }
};

export default {
  encryptAES,
  decryptAES
};